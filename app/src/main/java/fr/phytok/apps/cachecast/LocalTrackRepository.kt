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
import java.util.concurrent.ExecutorService
import javax.inject.Inject

class LocalTrackRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val videoDao: VideoDao,
    private val executorService: ExecutorService
) {

    fun searchTrack(): List<Track> {

        var tracks = listOf<Track>()
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
            MediaStore.Audio.Media.DISPLAY_NAME,
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
            val nameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                // Get values of columns for a given track.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                Log.d(TAG, "Reading track $name")

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                tracks += Track(contentUri, name, duration, size)
            }
        }
        return tracks
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