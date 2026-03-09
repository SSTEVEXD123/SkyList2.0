package com.skylist.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.skylist.app.data.models.Song

class PlayerManager(context: Context) {
    val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()
    private var queue: List<Song> = emptyList()

    fun setQueue(songs: List<Song>, startIndex: Int) {
        queue = songs
        player.setMediaItems(songs.map { MediaItem.fromUri(it.contentUri) }, startIndex, 0L)
        player.prepare()
    }

    fun currentSong(): Song? = queue.getOrNull(player.currentMediaItemIndex)

    fun release() = player.release()
}
