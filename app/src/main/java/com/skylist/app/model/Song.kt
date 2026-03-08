package com.skylist.app.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArt: Uri?,
    val contentUri: Uri,
    val durationMs: Long
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)
