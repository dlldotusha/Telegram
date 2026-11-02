package org.nextgram.engine.core

import java.util.Locale

/** Цвета в конфиге хранятся строкой "#AARRGGBB" (или "#RRGGBB"), чтобы файл было удобно читать руками. */
object Colors {

    fun format(argb: Int): String = String.format(Locale.US, "#%08X", argb)

    fun parse(value: Any?): Int? {
        if (value is Number) return value.toInt()
        if (value !is String) return null
        val s = value.trim().removePrefix("#")
        if (s.length != 6 && s.length != 8) return null
        var v = s.toLongOrNull(16) ?: return null
        if (s.length == 6) v = v or 0xFF000000L
        return v.toInt()
    }
}
