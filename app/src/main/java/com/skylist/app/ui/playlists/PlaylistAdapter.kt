package com.skylist.app.ui.playlists

import com.skylist.app.data.models.Playlist

class PlaylistAdapter {
    private var playlists: List<Playlist> = emptyList()

    fun submit(items: List<Playlist>) {
        playlists = items
    }

    fun count(): Int = playlists.size
}
