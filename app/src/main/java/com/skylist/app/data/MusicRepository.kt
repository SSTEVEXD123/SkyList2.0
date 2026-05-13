package com.skylist.app.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.skylist.app.lyrics.LrcParser
import com.skylist.app.model.Song
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MusicRepository(private val context: Context) {

    fun loadSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
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
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val albumId = cursor.getLong(albumIdIndex)
                val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleIndex).orEmpty().ifBlank { "Unknown title" },
                        artist = cursor.getString(artistIndex).orEmpty().ifBlank { "Unknown artist" },
                        album = cursor.getString(albumIndex).orEmpty().ifBlank { "Unknown album" },
                        albumArt = albumArtUri,
                        contentUri = songUri,
                        lyrics = readLyrics(songUri)
                    )
                )
            }
        }
        return songs
    }

    fun exportLibrary(songs: List<Song>): File {
        val backupDir = File(context.filesDir, "library_backups").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(backupDir, "skylist-library-$timestamp.csv")
        file.printWriter().use { writer ->
            writer.println("id,title,artist,album,uri")
            songs.forEach { song ->
                writer.println(
                    listOf(
                        song.id.toString(),
                        song.title,
                        song.artist,
                        song.album,
                        song.contentUri.toString()
                    ).joinToString(",") { it.toCsvCell() }
                )
            }
        }
        return file
    }

    private fun String.toCsvCell(): String = "\"${replace("\"", "\"\"")}\""

    private fun readLyrics(songUri: Uri) = runCatching {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, songUri)
        val rawLyrics = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LYRIC).orEmpty()
        retriever.release()
        LrcParser.parse(rawLyrics)
    }.getOrDefault(emptyList())
}
