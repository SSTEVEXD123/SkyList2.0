package com.skylist.app.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.skylist.app.R
import com.skylist.app.data.models.Song
import com.skylist.app.databinding.ItemSongBinding

class SongAdapter(private val onSongClick: (Int) -> Unit) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private var songs: List<Song> = emptyList()

    fun submitList(items: List<Song>) {
        songs = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) = holder.bind(songs[position], position)

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song, position: Int) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songCover.load(song.albumArt) {
                placeholder(R.drawable.ic_music_note)
                error(R.drawable.ic_music_note)
            }
            binding.root.setOnClickListener { onSongClick(position) }
        }
    }
}
