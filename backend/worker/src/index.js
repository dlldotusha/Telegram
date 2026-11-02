// NextGram profiles — Cloudflare Worker.
//
// Профили «для своих»: расширения профиля, которые видят только пользователи NextGram.
//
// Как клиент доказывает свой Telegram ID:
//   1. POST /v1/auth/start            → { nonce, bot }
//   2. клиент от имени пользователя запускает бота: /start ng_<nonce>
//      (Telegram сам подставляет настоящий from.id — подделать его нельзя)
//   3. бот (вебхук /tg/webhook) связывает nonce с from.id и выдаёт токен
//   4. GET  /v1/auth/poll?nonce=...   → { token, user_id }
//
// Дальше с заголовком Authorization: Bearer <token>:
//   GET/PUT/DELETE /v1/me             свой профиль
//   POST /v1/profiles { ids: [...] }  профили других (только для своих — список
//                                     пользователей NextGram наружу не отдаём)
//
// Хранилище — KV (binding NG_KV):
//   n:<nonce>        { status, uid?, token? }   TTL 10 мин
//   t:<sha256(tok)>  { uid }
//   u:<uid>          sha256 текущего токена (чтобы при переподключении старый отзывался)
//   p:<uid>          профиль

const NONCE_TTL = 600;
const MAX_IDS = 100;
const MAX_PROFILE_BYTES = 2048;
const PARTICLE_TYPES = ['stars', 'snow', 'bubbles', 'hearts'];

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    try {
      if (request.method === 'POST' && url.pathname === '/tg/webhook') return await webhook(request, env);
      if (request.method === 'POST' && url.pathname === '/v1/auth/start') return await authStart(env);
      if (request.method === 'GET' && url.pathname === '/v1/auth/poll') return await authPoll(url, env);
      if (url.pathname === '/v1/me') return await me(request, env);
      if (request.method === 'POST' && url.pathname === '/v1/profiles') return await profiles(request, env);
      if (url.pathname === '/' || url.pathname === '/health') return json({ ok: true, service: 'nextgram-profiles' });
      return json({ error: 'not_found' }, 404);
    } catch (e) {
      return json({ error: 'internal', message: String(e && e.message || e) }, 500);
    }
  },
};

// ------------------------------------------------------------------ auth

async function authStart(env) {
  const nonce = randomHex(16);
  await env.NG_KV.put('n:' + nonce, JSON.stringify({ status: 'pending' }), { expirationTtl: NONCE_TTL });
  return json({ nonce, bot: env.BOT_USERNAME });
}

async function authPoll(url, env) {
  const nonce = url.searchParams.get('nonce') || '';
  if (!/^[0-9a-f]{32}$/.test(nonce)) return json({ error: 'bad_nonce' }, 400);
  const raw = await env.NG_KV.get('n:' + nonce);
  if (!raw) return json({ error: 'expired' }, 410);
  const n = JSON.parse(raw);
  if (n.status !== 'ok') return json({ status: 'pending' });
  await env.NG_KV.delete('n:' + nonce); // токен отдаём ровно один раз
  return json({ status: 'ok', token: n.token, user_id: n.uid });
}

async function webhook(request, env) {
  if (request.headers.get('X-Telegram-Bot-Api-Secret-Token') !== env.WEBHOOK_SECRET) {
    return json({ error: 'forbidden' }, 403);
  }
  const update = await request.json();
  const msg = update.message;
  if (!msg || !msg.from || msg.from.is_bot || typeof msg.text !== 'string') return json({ ok: true });

  const m = msg.text.match(/^\/start(?:@\w+)?\s+ng_([0-9a-f]{32})$/);
  if (!m) {
    await reply(env, msg.chat.id, 'Это служебный бот NextGram. Подключение профиля запускается из приложения: Настройки → NextGram.');
    return json({ ok: true });
  }
  const key = 'n:' + m[1];
  const raw = await env.NG_KV.get(key);
  if (!raw || JSON.parse(raw).status !== 'pending') {
    await reply(env, msg.chat.id, 'Ссылка устарела. Нажмите «Подключить» в приложении ещё раз.');
    return json({ ok: true });
  }
  const uid = String(msg.from.id);
  const token = randomHex(32);
  const hash = await sha256(token);
  const old = await env.NG_KV.get('u:' + uid);
  if (old) await env.NG_KV.delete('t:' + old);
  await env.NG_KV.put('t:' + hash, JSON.stringify({ uid }));
  await env.NG_KV.put('u:' + uid, hash);
  await env.NG_KV.put(key, JSON.stringify({ status: 'ok', uid, token }), { expirationTtl: NONCE_TTL });
  await reply(env, msg.chat.id, '✅ Профиль NextGram подключён. Можно вернуться в приложение.');
  return json({ ok: true });
}

async function auth(request, env) {
  const h = request.headers.get('Authorization') || '';
  const m = h.match(/^Bearer ([0-9a-f]{64})$/);
  if (!m) return null;
  const raw = await env.NG_KV.get('t:' + await sha256(m[1]));
  return raw ? JSON.parse(raw).uid : null;
}

// ------------------------------------------------------------------ profiles

async function me(request, env) {
  const uid = await auth(request, env);
  if (!uid) return json({ error: 'unauthorized' }, 401);
  if (request.method === 'GET') {
    const raw = await env.NG_KV.get('p:' + uid);
    return json({ user_id: uid, profile: raw ? JSON.parse(raw) : null });
  }
  if (request.method === 'PUT') {
    const text = await request.text();
    if (text.length > MAX_PROFILE_BYTES) return json({ error: 'too_large' }, 413);
    const profile = sanitize(JSON.parse(text));
    if (!profile) return json({ error: 'bad_profile' }, 400);
    await env.NG_KV.put('p:' + uid, JSON.stringify(profile));
    return json({ user_id: uid, profile });
  }
  if (request.method === 'DELETE') {
    // Отключение: удаляем профиль и токен
    const hash = await env.NG_KV.get('u:' + uid);
    await env.NG_KV.delete('p:' + uid);
    if (hash) await env.NG_KV.delete('t:' + hash);
    await env.NG_KV.delete('u:' + uid);
    return json({ ok: true });
  }
  return json({ error: 'method' }, 405);
}

async function profiles(request, env) {
  const uid = await auth(request, env);
  if (!uid) return json({ error: 'unauthorized' }, 401);
  const body = await request.json();
  const ids = Array.isArray(body.ids) ? body.ids.map(String).filter((s) => /^\d{1,20}$/.test(s)).slice(0, MAX_IDS) : [];
  const out = {};
  await Promise.all(ids.map(async (id) => {
    const raw = await env.NG_KV.get('p:' + id);
    if (raw) out[id] = JSON.parse(raw);
  }));
  return json({ profiles: out });
}

/** Только известные поля в допустимых пределах — сервер не хранит произвольный мусор. */
function sanitize(p) {
  if (!p || typeof p !== 'object') return null;
  const out = { v: 1 };
  const pt = p.particles;
  if (pt && typeof pt === 'object' && PARTICLE_TYPES.includes(pt.type)) {
    out.particles = {
      type: pt.type,
      color: /^#[0-9A-Fa-f]{8}$/.test(pt.color) ? pt.color.toUpperCase() : '#FFFFFFFF',
      density: clamp(Number(pt.density) || 1, 0.3, 2),
    };
  }
  return out;
}

// ------------------------------------------------------------------ utils

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' },
  });
}

async function reply(env, chatId, text) {
  await fetch(`https://api.telegram.org/bot${env.BOT_TOKEN}/sendMessage`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ chat_id: chatId, text }),
  });
}

function randomHex(bytes) {
  const a = new Uint8Array(bytes);
  crypto.getRandomValues(a);
  return [...a].map((b) => b.toString(16).padStart(2, '0')).join('');
}

async function sha256(s) {
  const d = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(s));
  return [...new Uint8Array(d)].map((b) => b.toString(16).padStart(2, '0')).join('');
}

function clamp(v, a, b) {
  return Math.min(b, Math.max(a, v));
}
