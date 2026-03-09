package com.skylist.app.data.models

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArt: Uri?,
    val contentUri: Uri,
    val lyrics: List<LyricLine> = emptyList()
)

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class Playlist(
    val id: Long,
    val name: String,
    val songIds: MutableList<Long>
)

data class ListenTogetherSession(
    val code: String,
    val hostName: String,
    var nowPlayingSongId: Long? = null,
    var positionMs: Long = 0L
)
