package com.skylist.app.recognition

import com.skylist.app.data.models.Song

class MusicRecognizerManager {
    fun recognizeFromSnippet(snippetLabel: String, songs: List<Song>): Song? {
        val query = snippetLabel.lowercase()
        return songs.firstOrNull { song ->
            song.title.lowercase().contains(query) || song.artist.lowercase().contains(query)
        }
    }
}
