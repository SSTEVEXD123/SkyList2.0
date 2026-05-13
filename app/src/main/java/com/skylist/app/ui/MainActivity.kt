package com.skylist.app.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private var totalSongs = 0

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
            if (binding.librarySearch.query.isNullOrBlank()) totalSongs = songs.size
            adapter.submitList(songs)
            binding.libraryStatus.text = when {
                totalSongs == 0 && songs.isEmpty() -> getString(R.string.library_empty)
                binding.librarySearch.query.isNullOrBlank() -> getString(R.string.library_count, songs.size)
                else -> getString(R.string.library_filtered_count, songs.size, totalSongs)
            }
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

        viewModel.shuffleEnabled.observe(this) { enabled ->
            binding.shuffleButton.text = getString(if (enabled) R.string.shuffle_on else R.string.shuffle_off)
            binding.shuffleButton.isSelected = enabled
        }

        viewModel.sleepTimerMinutes.observe(this) { minutes ->
            binding.sleepTimerStatus.text = minutes?.let { getString(R.string.sleep_timer_active, it) }.orEmpty()
        }

        binding.librarySearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchSongs(query.orEmpty())
                binding.librarySearch.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchSongs(newText.orEmpty())
                return true
            }
        })

        binding.playPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.miniPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        binding.next.setOnClickListener { viewModel.next() }
        binding.previous.setOnClickListener { viewModel.previous() }
        binding.shuffleButton.setOnClickListener { viewModel.toggleShuffle() }
        binding.sleepTimerButton.setOnClickListener { showSleepTimerDialog() }
        binding.copyLyricsButton.setOnClickListener { copyLyrics() }
        binding.backupButton.setOnClickListener { exportLibrary() }

        binding.seekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.seekTo(progress.toLong())
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) = Unit
        })
    }

    private fun showSleepTimerDialog() {
        val labels = arrayOf("15 min", "30 min", "45 min", "60 min", getString(R.string.sleep_timer_cancel))
        val values = intArrayOf(15, 30, 45, 60)
        AlertDialog.Builder(this)
            .setTitle(R.string.sleep_timer_title)
            .setItems(labels) { _, which ->
                if (which < values.size) {
                    viewModel.startSleepTimer(values[which])
                } else {
                    viewModel.cancelSleepTimer()
                    Toast.makeText(this, R.string.sleep_timer_off, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun copyLyrics() {
        val lyrics = viewModel.copyableLyrics()
        if (lyrics.isBlank()) {
            Toast.makeText(this, R.string.lyrics_empty, Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.copy_lyrics), lyrics))
        Toast.makeText(this, R.string.lyrics_copied, Toast.LENGTH_SHORT).show()
    }

    private fun exportLibrary() {
        runCatching { viewModel.exportLibrary() }
            .onSuccess { file -> Toast.makeText(this, getString(R.string.backup_created, file.name), Toast.LENGTH_LONG).show() }
            .onFailure { Toast.makeText(this, R.string.backup_failed, Toast.LENGTH_SHORT).show() }
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
