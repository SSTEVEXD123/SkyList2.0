package com.skylist.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.skylist.app.model.Song

class PlayerController(context: Context) {
    private val appContext = context.applicationContext
    val player: ExoPlayer = ExoPlayer.Builder(appContext).build()
    private var playlist: List<Song> = emptyList()

    fun setQueue(songs: List<Song>, startIndex: Int) {
        playlist = songs
        player.setMediaItems(songs.map { MediaItem.fromUri(it.contentUri) }, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true
    }

    fun currentSong(): Song? = playlist.getOrNull(player.currentMediaItemIndex)

    fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() = player.seekToNextMediaItem()

    fun previous() = player.seekToPreviousMediaItem()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun release() = player.release()
}
