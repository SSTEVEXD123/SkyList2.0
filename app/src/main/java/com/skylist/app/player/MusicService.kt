package com.skylist.app.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.skylist.app.ui.main.MainActivity

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val manager = ServiceLocator.getPlayerManager(applicationContext)
        val sessionIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        mediaSession = MediaSession.Builder(this, manager.player)
            .setSessionActivity(sessionIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.player?.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}

object ServiceLocator {
    @Volatile
    private var playerManager: PlayerManager? = null

    fun getPlayerManager(context: android.content.Context): PlayerManager {
        return playerManager ?: synchronized(this) {
            playerManager ?: PlayerManager(context).also { playerManager = it }
        }
    }
}
