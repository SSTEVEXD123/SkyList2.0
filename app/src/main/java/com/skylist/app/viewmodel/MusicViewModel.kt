package com.skylist.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.skylist.app.data.MusicRepository
import com.skylist.app.model.Song
import com.skylist.app.player.PlayerController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    private val _activeLyric = MutableLiveData("")
    val activeLyric: LiveData<String> = _activeLyric

    private var loadSongsJob: Job? = null
    private var progressJob: Job? = null

    private var queue: List<Song> = emptyList()
    private var currentIndex: Int = -1

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.postValue(isPlaying)
            if (isPlaying) {
                startProgressUpdates()
            } else {
                stopProgressUpdates()
                publishPlaybackState()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrentFromPlayer()
            publishPlaybackState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            publishPlaybackState()
            if (playbackState == Player.STATE_ENDED) {
                stopProgressUpdates()
            }
        }
    }

    init {
        playerController.player.addListener(playerListener)
    }

    fun loadSongs() {
        if (loadSongsJob?.isActive == true) return
        loadSongsJob = viewModelScope.launch(Dispatchers.IO) {
            val loadedSongs = repository.loadSongs()
            queue = loadedSongs
            _songs.postValue(loadedSongs)

            if (loadedSongs.isNotEmpty() && currentIndex == -1) {
                currentIndex = 0
                _currentSong.postValue(loadedSongs.first())
                _duration.postValue(loadedSongs.first().durationMs)
            }
        }
    }

    fun playSong(index: Int) {
        val list = _songs.value.orEmpty()
        if (list.isEmpty() || index !in list.indices) return

        queue = list
        currentIndex = index
        playerController.setQueue(queue, currentIndex)
        publishPlaybackState()
        startProgressUpdates()
    }

    fun togglePlayPause() {
        if (!playerController.hasQueue()) {
            val list = _songs.value.orEmpty()
            if (list.isEmpty()) return

            queue = list
            if (currentIndex !in list.indices) {
                currentIndex = 0
            }
            playerController.setQueue(queue, currentIndex)
            publishPlaybackState()
            startProgressUpdates()
            return
        }

        playerController.playPause()
    }

    fun next() {
        if (!playerController.hasQueue()) return
        playerController.next()
        syncCurrentFromPlayer()
        publishPlaybackState()
    }

    fun previous() {
        if (!playerController.hasQueue()) return
        playerController.previous()
        syncCurrentFromPlayer()
        publishPlaybackState()
    }

    fun seekTo(positionMs: Long) {
        if (!playerController.hasQueue()) return
        playerController.seekTo(positionMs)
        publishPlaybackState()
    }

    private fun syncCurrentFromPlayer() {
        val index = playerController.currentIndex()
        if (index in queue.indices) {
            currentIndex = index
        }
    }

    private fun startProgressUpdates() {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (isActive) {
                publishPlaybackState()
                delay(250)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun publishPlaybackState() {
        val song = when {
            playerController.hasQueue() -> playerController.currentSong()
            currentIndex in queue.indices -> queue[currentIndex]
            else -> null
        }

        _currentSong.postValue(song)
        _isPlaying.postValue(playerController.isPlaying())

        val positionMs = if (playerController.hasQueue()) {
            playerController.currentPosition()
        } else {
            0L
        }
        _position.postValue(positionMs)

        val durationMs = song?.durationMs ?: 0L
        _duration.postValue(durationMs)
        _activeLyric.postValue("")
    }

    override fun onCleared() {
        loadSongsJob?.cancel()
        stopProgressUpdates()
        playerController.player.removeListener(playerListener)
        playerController.release()
        super.onCleared()
    }
}
