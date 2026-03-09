package com.skylist.app.lyrics

import com.skylist.app.data.models.LyricLine

class LyricsManager {
    fun activeLine(lines: List<LyricLine>, positionMs: Long): String {
        return lines.lastOrNull { it.timestampMs <= positionMs }?.text.orEmpty()
    }

    fun mergeEmbeddedAndApi(embedded: List<LyricLine>, apiRaw: String?): List<LyricLine> {
        if (embedded.isNotEmpty()) return embedded
        return apiRaw?.let { LyricsParser.parse(it) }.orEmpty()
    }
}
