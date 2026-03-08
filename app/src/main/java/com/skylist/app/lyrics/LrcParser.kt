package com.skylist.app.lyrics

import com.skylist.app.model.LyricLine

object LrcParser {
    private val regex = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{1,3}))?]([^\\n]*)")

    fun parse(raw: String): List<LyricLine> {
        return raw.lines().mapNotNull { line ->
            regex.find(line)?.let { match ->
                val min = match.groupValues[1].toLongOrNull() ?: return@let null
                val sec = match.groupValues[2].toLongOrNull() ?: return@let null
                val ms = (match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L)
                LyricLine(timestampMs = (min * 60 + sec) * 1000 + ms, text = match.groupValues[4].trim())
            }
        }.sortedBy { it.timestampMs }
    }
}
