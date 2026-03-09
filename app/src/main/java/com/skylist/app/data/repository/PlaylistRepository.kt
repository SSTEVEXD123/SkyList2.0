package com.skylist.app.data.repository

import com.skylist.app.data.models.Playlist

class PlaylistRepository {
    private val playlists = mutableListOf(
        Playlist(id = 1, name = "Favorites", songIds = mutableListOf()),
        Playlist(id = 2, name = "Listen Later", songIds = mutableListOf())
    )

    fun getPlaylists(): List<Playlist> = playlists

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlists.find { it.id == playlistId }?.songIds?.add(songId)
    }
}
