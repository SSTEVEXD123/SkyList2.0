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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)
    val playerController = PlayerController(application)

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

    fun loadSongs() {
        _songs.value = repository.loadSongs()
    }

    fun playSong(index: Int) {
        val list = _songs.value.orEmpty()
        if (list.isEmpty()) return
        playerController.setQueue(list, index)
        observePlayer()
    }

    fun togglePlayPause() = playerController.playPause()
    fun next() = playerController.next()
    fun previous() = playerController.previous()
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)

    private fun observePlayer() {
        viewModelScope.launch {
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
        playerController.release()
        super.onCleared()
    }
}
