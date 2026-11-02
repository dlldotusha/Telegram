package org.telegram.custom.core;

import java.util.Locale;

/** Цвета в конфиге хранятся строкой "#AARRGGBB" (или "#RRGGBB"), чтобы файл было удобно читать руками. */
public final class Colors {

    private Colors() {
    }

    public static String format(int argb) {
        return String.format(Locale.US, "#%08X", argb);
    }

    public static Integer parse(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (!(value instanceof String)) {
            return null;
        }
        String s = ((String) value).trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (s.length() != 6 && s.length() != 8) {
            return null;
        }
        try {
            long v = Long.parseLong(s, 16);
            if (s.length() == 6) {
                v |= 0xFF000000L;
            }
            return (int) v;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
