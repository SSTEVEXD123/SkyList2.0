package com.skylist.app.discord

import com.skylist.app.data.models.Song

class DiscordRPCManager {
    private var latestPresence: String = "Idle"

    fun updatePresence(song: Song?, isPlaying: Boolean): String {
        latestPresence = if (song == null) {
            "Browsing SkyList"
        } else {
            val state = if (isPlaying) "Listening" else "Paused"
            "$state: ${song.title} — ${song.artist}"
        }
        return latestPresence
    }

    fun currentPresence(): String = latestPresence
}
