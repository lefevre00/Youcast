package fr.phytok.apps.cachecast

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.phytok.apps.cachecast.yas.VideoSnippet
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocalTrackRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    fun searchTrack(): Unit {

        val contentResolver = applicationContext.contentResolver

        // Need the READ_EXTERNAL_STORAGE permission if accessing video files that your
// app didn't create.

        // Container for information about each video.
        data class Audio(val uri: Uri,
                         val name: String,
                         val duration: Int,
                         val size: Int
        )
        val tracks = mutableListOf<Audio>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )

// Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString()
        )

// Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                tracks += Audio(contentUri, name, duration, size)
            }
        }
    }

    fun saveTrack(bytes: ByteArray, track: VideoSnippet) {
        // Add a media item that other apps shouldn't see until the item is
// fully written to the media store.
        val resolver = applicationContext.contentResolver

// Find all audio files on the primary external storage device.
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val songDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "${track.title}.mp3")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val songContentUri = resolver.insert(audioCollection, songDetails)!!

        resolver.openFileDescriptor(songContentUri, "w", null)?.use { pfd ->
            FileOutputStream(pfd.fileDescriptor).use {
                it.write(bytes)
            }
        }

        // Now that we're finished, release the "pending" status, and allow other apps
        // to play the audio track.
        songDetails.clear()
        songDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(songContentUri, songDetails, null, null)
    }
}