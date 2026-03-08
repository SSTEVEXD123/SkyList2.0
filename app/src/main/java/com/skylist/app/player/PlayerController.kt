package com.skylist.app.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.skylist.app.model.Song

class PlayerController(context: Context) {
    private val appContext = context.applicationContext
    val player: ExoPlayer = ExoPlayer.Builder(appContext).build()
    private var playlist: List<Song> = emptyList()

    init {
        player.repeatMode = Player.REPEAT_MODE_ALL
    }

    fun setQueue(songs: List<Song>, startIndex: Int, startPositionMs: Long = 0L) {
        playlist = songs
        player.setMediaItems(songs.map { MediaItem.fromUri(it.contentUri) }, startIndex, startPositionMs.coerceAtLeast(0L))
        player.prepare()
        player.play()
    }

    fun hasQueue(): Boolean = playlist.isNotEmpty()

    fun setPlayWhenReady(playWhenReady: Boolean) {
        player.playWhenReady = playWhenReady
    }

    fun currentSong(): Song? = playlist.getOrNull(player.currentMediaItemIndex)

    fun currentIndex(): Int = player.currentMediaItemIndex

    fun currentPosition(): Long = player.currentPosition.coerceAtLeast(0L)

    fun isPlaying(): Boolean = player.isPlaying

    fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() = player.seekToNextMediaItem()

    fun previous() = player.seekToPreviousMediaItem()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs.coerceAtLeast(0L))

    fun release() = player.release()
}
