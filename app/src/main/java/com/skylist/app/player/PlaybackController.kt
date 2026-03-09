package com.skylist.app.player

class PlaybackController(private val playerManager: PlayerManager) {
    fun togglePlayPause() {
        val player = playerManager.player
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() = playerManager.player.seekToNextMediaItem()

    fun previous() = playerManager.player.seekToPreviousMediaItem()

    fun seekTo(positionMs: Long) = playerManager.player.seekTo(positionMs)
}
