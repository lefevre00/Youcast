package fr.phytok.apps.cachecast

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.phytok.apps.cachecast.db.VideoDao
import fr.phytok.apps.cachecast.model.Track
import fr.phytok.apps.cachecast.model.TrackDto
import java.io.File
import java.util.concurrent.ExecutorService
import javax.inject.Inject

class LocalTrackRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val videoDao: VideoDao,
    private val executorService: ExecutorService
) {

    fun searchTrack(): List<Track> {

        val tracks = mutableListOf<Track>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        // TODO : refresh provider for deleted files

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )

        // Show only tracks that are at least 5 minutes in duration.
        val selection = ""
        val selectionArgs = emptyArray<String>()

        // Display tracks in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        query?.use {
                cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given track.
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val channel = cursor.getString(artistColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                Log.d(TAG, "Reading track $title")

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                if (isNonEmptyFile(contentUri)) {
                    tracks += Track(contentUri, title, channel, duration, size)
                }
            }
        }
        return tracks
    }

    private fun isNonEmptyFile(contentUri: Uri): Boolean {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        applicationContext.contentResolver.query(contentUri, projection, null, null, null)?.let { cursor ->
            cursor.use {
                if (cursor.moveToFirst()) {
                    val filePath = cursor.getString(0);
                    val file = File(filePath)
                    if (!file.exists()) {
                        Log.w(TAG, "File does not exist: $contentUri")
                        return false
                    }
                    if (file.length() == 0L) {
                        Log.w(TAG, "File is empty: $contentUri")
                        return false
                    }
                    Log.d(TAG, "File is not empty: $contentUri")
                    return true
                } else {
                    Log.w(TAG, "No file path for $contentUri")
                }
            }
        } ?: run {
            Log.d(TAG, "No cursor fetched for $contentUri")
        }

        return false
    }

    fun save(track: TrackDto) {
        executorService.execute { videoDao.insert(track.toDbTrack()) }
    }

    fun exists(videoId: String) : Boolean {
        val future = executorService.submit<Boolean> {
            return@submit videoDao.firstByVideoId(videoId)?.let { true } ?: false
        }
        return future.get()
    }

    companion object {
        private const val TAG = "LocalTrackRepository"
    }
}