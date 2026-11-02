package org.nextgram.profiles;

import org.json.JSONException;
import org.json.JSONObject;
import org.nextgram.engine.core.Colors;

/** Расширенный профиль NextGram. Формат совпадает с тем, что принимает сервер (backend/worker). */
public final class NgProfile {

    public static final String[] PARTICLE_TYPES = {"stars", "snow", "bubbles", "hearts"};

    /** null — частиц нет. */
    public String particles;
    public int particlesColor = 0xFFFFFFFF;
    public float density = 1f;

    public boolean isEmpty() {
        return particles == null;
    }

    public NgProfile copy() {
        NgProfile p = new NgProfile();
        p.particles = particles;
        p.particlesColor = particlesColor;
        p.density = density;
        return p;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("v", 1);
            if (particles != null) {
                JSONObject pt = new JSONObject();
                pt.put("type", particles);
                pt.put("color", Colors.format(particlesColor));
                pt.put("density", (double) density);
                o.put("particles", pt);
            }
        } catch (JSONException ignore) {
        }
        return o;
    }

    public static NgProfile fromJson(JSONObject o) {
        if (o == null) {
            return null;
        }
        NgProfile p = new NgProfile();
        JSONObject pt = o.optJSONObject("particles");
        if (pt != null) {
            String type = pt.optString("type", null);
            for (String t : PARTICLE_TYPES) {
                if (t.equals(type)) {
                    p.particles = t;
                }
            }
            Integer c = Colors.parse(pt.optString("color", null));
            if (c != null) {
                p.particlesColor = c;
            }
            p.density = (float) Math.max(0.3, Math.min(2.0, pt.optDouble("density", 1.0)));
        }
        return p;
    }
}
