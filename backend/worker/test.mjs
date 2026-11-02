// Тест воркера без Cloudflare: KV и Telegram заменены заглушками.
// Запуск: node backend/worker/test.mjs
import worker from './src/index.js';

const store = new Map();
const env = {
  BOT_USERNAME: 'NextGramTestBot',
  BOT_TOKEN: 'x',
  WEBHOOK_SECRET: 's3cret',
  NG_KV: {
    async get(k) { return store.has(k) ? store.get(k) : null; },
    async put(k, v) { store.set(k, v); },
    async delete(k) { store.delete(k); },
  },
};
const sent = [];
globalThis.fetch = async (url, init) => { sent.push({ url, body: init && init.body }); return new Response('{}'); };

const call = (method, path, body, headers = {}) =>
  worker.fetch(new Request('https://w.example' + path, { method, headers, body: body == null ? undefined : (typeof body === 'string' ? body : JSON.stringify(body)) }), env)
    .then(async (r) => ({ status: r.status, data: await r.json() }));

let failed = 0;
const check = (c, name) => { if (!c) { failed++; console.log('FAIL', name); } };

const start = await call('POST', '/v1/auth/start');
check(start.status === 200 && /^[0-9a-f]{32}$/.test(start.data.nonce), 'auth/start');
check(start.data.bot === 'NextGramTestBot', 'bot username');

let poll = await call('GET', '/v1/auth/poll?nonce=' + start.data.nonce);
check(poll.data.status === 'pending', 'pending before bot');

const hook = (text, secret = 's3cret', from = { id: 42, is_bot: false }) => call('POST', '/tg/webhook',
  { message: { text, from, chat: { id: 42 } } }, { 'X-Telegram-Bot-Api-Secret-Token': secret });
check((await hook('/start ng_' + start.data.nonce, 'wrong')).status === 403, 'webhook secret');
check((await hook('/start ng_' + start.data.nonce)).status === 200, 'webhook ok');

poll = await call('GET', '/v1/auth/poll?nonce=' + start.data.nonce);
check(poll.data.status === 'ok' && poll.data.user_id === '42' && /^[0-9a-f]{64}$/.test(poll.data.token), 'poll ok');
check((await call('GET', '/v1/auth/poll?nonce=' + start.data.nonce)).status === 410, 'token issued once');

const auth = { Authorization: 'Bearer ' + poll.data.token };
check((await call('GET', '/v1/me')).status === 401, 'me needs auth');
const put = await call('PUT', '/v1/me', { particles: { type: 'snow', color: '#ff112233', density: 9 }, junk: 1 }, auth);
check(put.status === 200 && put.data.profile.particles.type === 'snow' && put.data.profile.particles.density === 2 && put.data.profile.junk === undefined, 'sanitize');
check(put.data.profile.particles.color === '#FF112233', 'color normalized');
check((await call('PUT', '/v1/me', { particles: { type: 'lasers' } }, auth)).data.profile.particles === undefined, 'unknown type dropped');
await call('PUT', '/v1/me', { particles: { type: 'stars', color: '#FFFFFFFF' } }, auth);

const prof = await call('POST', '/v1/profiles', { ids: [42, 7, 'x', '1;drop'] }, auth);
check(prof.status === 200 && prof.data.profiles['42'].particles.type === 'stars' && !('7' in prof.data.profiles), 'batch');
check((await call('POST', '/v1/profiles', { ids: [42] })).status === 401, 'batch needs auth');

// повторное подключение отзывает старый токен
const s2 = await call('POST', '/v1/auth/start');
await hook('/start ng_' + s2.data.nonce);
const p2 = await call('GET', '/v1/auth/poll?nonce=' + s2.data.nonce);
check((await call('GET', '/v1/me', null, auth)).status === 401, 'old token revoked');
check((await call('GET', '/v1/me', null, { Authorization: 'Bearer ' + p2.data.token })).status === 200, 'new token works');

check((await call('DELETE', '/v1/me', null, { Authorization: 'Bearer ' + p2.data.token })).status === 200, 'delete');
check((await call('GET', '/v1/me', null, { Authorization: 'Bearer ' + p2.data.token })).status === 401, 'token gone after delete');

await hook('hello');
check(sent.length > 0 && sent.every((s) => s.url.startsWith('https://api.telegram.org/botx/sendMessage')), 'bot replies');

console.log(failed ? `${failed} failed` : 'all passed');
process.exit(failed ? 1 : 0);
