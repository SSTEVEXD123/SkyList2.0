package com.skylist.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.skylist.app.data.MusicRepository
import com.skylist.app.model.LyricLine
import com.skylist.app.model.Song
import com.skylist.app.player.PlayerController
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)
    val playerController = PlayerController(application)

    private var allSongs: List<Song> = emptyList()
    private var query: String = ""
    private var observeJob: Job? = null
    private var sleepTimerJob: Job? = null

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

    private val _activeLyric = MutableLiveData<String>("")
    val activeLyric: LiveData<String> = _activeLyric

    private val _shuffleEnabled = MutableLiveData(false)
    val shuffleEnabled: LiveData<Boolean> = _shuffleEnabled

    private val _sleepTimerMinutes = MutableLiveData<Int?>(null)
    val sleepTimerMinutes: LiveData<Int?> = _sleepTimerMinutes

    fun loadSongs() {
        allSongs = repository.loadSongs()
        applySearch(query)
        if (allSongs.isNotEmpty()) {
            repository.exportLibrary(allSongs)
        }
    }

    fun searchSongs(newQuery: String) {
        query = newQuery.trim()
        applySearch(query)
    }

    fun playSong(index: Int) {
        val list = _songs.value.orEmpty()
        if (list.isEmpty()) return
        if (_shuffleEnabled.value == true) {
            val queue = listOf(list[index]) + (list.take(index) + list.drop(index + 1)).shuffled()
            playerController.setQueue(queue, 0)
        } else {
            playerController.setQueue(list, index)
        }
        observePlayer()
    }

    fun toggleShuffle() {
        val enabled = _shuffleEnabled.value != true
        _shuffleEnabled.value = enabled
        playerController.player.shuffleModeEnabled = enabled
    }

    fun togglePlayPause() = playerController.playPause()
    fun next() = playerController.next()
    fun previous() = playerController.previous()
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)

    fun copyableLyrics(): String = currentSong.value?.lyrics
        .orEmpty()
        .joinToString(separator = "\n") { it.text }
        .ifBlank { activeLyric.value.orEmpty() }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        sleepTimerJob = viewModelScope.launch {
            delay(minutes * 60_000L)
            playerController.player.pause()
            _sleepTimerMinutes.postValue(null)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutes.value = null
    }

    fun exportLibrary(): File = repository.exportLibrary(allSongs)

    private fun applySearch(searchQuery: String) {
        val filteredSongs = if (searchQuery.isBlank()) {
            allSongs
        } else {
            allSongs.filter { song ->
                song.title.contains(searchQuery, ignoreCase = true) ||
                    song.artist.contains(searchQuery, ignoreCase = true) ||
                    song.album.contains(searchQuery, ignoreCase = true)
            }
        }
        _songs.value = filteredSongs
    }

    private fun observePlayer() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            while (isActive) {
                val player = playerController.player
                _isPlaying.postValue(player.isPlaying)
                _position.postValue(player.currentPosition)
                _duration.postValue(if (player.duration > 0) player.duration else 0)
                val song = playerController.currentSong()
                _currentSong.postValue(song)
                _activeLyric.postValue(resolveLine(song?.lyrics.orEmpty(), player.currentPosition))
                delay(300)
            }
        }
    }

    private fun resolveLine(lines: List<LyricLine>, position: Long): String {
        if (lines.isEmpty()) return ""
        return lines.lastOrNull { it.timestampMs <= position }?.text.orEmpty()
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        observeJob?.cancel()
        playerController.release()
        super.onCleared()
    }
}
