package com.skylist.app.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skylist.app.data.models.ListenTogetherSession
import com.skylist.app.data.models.Song
import com.skylist.app.data.repository.ListenTogetherRepository
import com.skylist.app.data.repository.MusicRepository
import com.skylist.app.data.repository.PlaylistRepository
import com.skylist.app.discord.DiscordRPCManager
import com.skylist.app.lyrics.LyricsManager
import com.skylist.app.player.PlaybackController
import com.skylist.app.player.ServiceLocator
import com.skylist.app.recognition.MusicRecognizerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val musicRepository = MusicRepository(application)
    private val playlistRepository = PlaylistRepository()
    private val lyricsManager = LyricsManager()
    private val playerManager = ServiceLocator.getPlayerManager(application)
    private val playbackController = PlaybackController(playerManager)
    private val discordRPCManager = DiscordRPCManager()
    private val musicRecognizerManager = MusicRecognizerManager()
    private val listenTogetherRepository = ListenTogetherRepository()

    private val _songs = MutableLiveData<List<Song>>(emptyList())
    val songs: LiveData<List<Song>> = _songs

    private val _currentSong = MutableLiveData<Song?>(null)
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _position = MutableLiveData(0L)
    val position: LiveData<Long> = _position

    private val _duration = MutableLiveData(0L)
    val duration: LiveData<Long> = _duration

    private val _activeLyric = MutableLiveData("")
    val activeLyric: LiveData<String> = _activeLyric

    private val _discordStatus = MutableLiveData("Idle")
    val discordStatus: LiveData<String> = _discordStatus

    private val _recognitionStatus = MutableLiveData("Recognition idle")
    val recognitionStatus: LiveData<String> = _recognitionStatus

    private val _listenTogether = MutableLiveData<ListenTogetherSession?>(null)
    val listenTogether: LiveData<ListenTogetherSession?> = _listenTogether

    fun loadSongs() {
        _songs.value = musicRepository.loadSongs()
    }

    fun playSong(index: Int) {
        val list = _songs.value.orEmpty()
        if (list.isEmpty()) return
        playerManager.setQueue(list, index)
        playerManager.player.playWhenReady = true
        observePlayer()
    }

    fun togglePlayPause() = playbackController.togglePlayPause()
    fun next() = playbackController.next()
    fun previous() = playbackController.previous()
    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)

    fun hostListenTogetherSession(host: String = "SSteveXD") {
        _listenTogether.value = listenTogetherRepository.hostSession(host)
    }

    fun simulateRecognition(query: String) {
        val match = musicRecognizerManager.recognizeFromSnippet(query, _songs.value.orEmpty())
        _recognitionStatus.value = match?.let { "Matched: ${it.title}" } ?: "No match for '$query'"
    }

    fun addCurrentSongToFavorites() {
        val song = _currentSong.value ?: return
        playlistRepository.addSongToPlaylist(1, song.id)
    }

    private fun observePlayer() {
        viewModelScope.launch {
            while (isActive) {
                val player = playerManager.player
                val song = playerManager.currentSong()
                _isPlaying.postValue(player.isPlaying)
                _position.postValue(player.currentPosition)
                _duration.postValue(if (player.duration > 0) player.duration else 0)
                _currentSong.postValue(song)
                _activeLyric.postValue(
                    lyricsManager.activeLine(song?.lyrics.orEmpty(), player.currentPosition)
                )
                _discordStatus.postValue(discordRPCManager.updatePresence(song, player.isPlaying))
                song?.id?.let { listenTogetherRepository.sync(it, player.currentPosition) }
                delay(300)
            }
        }
    }

    override fun onCleared() {
        playerManager.release()
        super.onCleared()
    }
}
