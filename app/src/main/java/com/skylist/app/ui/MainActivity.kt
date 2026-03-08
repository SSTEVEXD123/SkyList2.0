package com.skylist.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.skylist.app.R
import com.skylist.app.databinding.ActivityMainBinding
import com.skylist.app.viewmodel.MusicViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MusicViewModel by viewModels()
    private val adapter = SongAdapter { index -> viewModel.playSong(index) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.loadSongs() else binding.permissionHint.isVisible = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.songList.layoutManager = LinearLayoutManager(this)
        binding.songList.adapter = adapter
        bindUi()
        askPermissionAndLoad()
    }

    private fun bindUi() {
        viewModel.songs.observe(this) { songs ->
            adapter.submitList(songs)
        }

        viewModel.currentSong.observe(this) { song ->
            binding.miniPlayerRoot.isVisible = song != null
            binding.miniTitle.text = song?.title.orEmpty()
            binding.miniArtist.text = song?.artist.orEmpty()
            binding.playerSongTitle.text = song?.title.orEmpty()
            binding.playerSongArtist.text = song?.artist.orEmpty()
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.playPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            binding.miniPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

        viewModel.position.observe(this) { position ->
            binding.seekBar.progress = position.toInt()
        }

        viewModel.duration.observe(this) { duration ->
            binding.seekBar.max = duration.toInt()
        }

        viewModel.activeLyric.observe(this) { line ->
            binding.lyricsLine.text = if (line.isBlank()) getString(R.string.no_lyrics) else line
        }

        binding.playPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.miniPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.next.setOnClickListener { viewModel.next() }
        binding.previous.setOnClickListener { viewModel.previous() }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
        })
    }

    private fun askPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadSongs()
        } else {
            permissionLauncher.launch(permission)
        }
    }
}
