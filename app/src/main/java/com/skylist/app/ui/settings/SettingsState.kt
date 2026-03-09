package com.skylist.app.ui.settings

data class SettingsState(
    val discordPresenceEnabled: Boolean = true,
    val synchronizedLyricsEnabled: Boolean = true,
    val listenTogetherEnabled: Boolean = true,
    val musicRecognitionEnabled: Boolean = true
)
