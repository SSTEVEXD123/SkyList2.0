package com.skylist.app.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.skylist.app.lyrics.LrcParser
import com.skylist.app.model.Song

class MusicRepository(private val context: Context) {

    fun loadSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleIndex),
                        artist = cursor.getString(artistIndex) ?: "Unknown",
                        albumArt = albumArtUri,
                        contentUri = songUri,
                        lyrics = readLyrics(songUri)
                    )
                )
            }
        }
        return songs
    }

    private fun readLyrics(songUri: Uri) = runCatching {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, songUri)
        val rawLyrics = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LYRIC).orEmpty()
        retriever.release()
        LrcParser.parse(rawLyrics)
    }.getOrDefault(emptyList())
}
