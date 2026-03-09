package com.skylist.app.data.repository

import com.skylist.app.data.models.ListenTogetherSession
import java.util.UUID

class ListenTogetherRepository {
    private var currentSession: ListenTogetherSession? = null

    fun hostSession(hostName: String): ListenTogetherSession {
        val session = ListenTogetherSession(
            code = UUID.randomUUID().toString().take(6).uppercase(),
            hostName = hostName
        )
        currentSession = session
        return session
    }

    fun joinSession(code: String): ListenTogetherSession? {
        return currentSession?.takeIf { it.code == code.uppercase() }
    }

    fun sync(songId: Long, positionMs: Long) {
        currentSession?.nowPlayingSongId = songId
        currentSession?.positionMs = positionMs
    }
}
