package fr.phytok.apps.cachecast.yas

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.phytok.apps.cachecast.db.VideoDao
import fr.phytok.apps.cachecast.services.DownloadRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.concurrent.ExecutorService
import javax.inject.Inject

class RemoteTrackRepository @Inject constructor(
    private val yasClient: YasApiClient,
    @ApplicationContext private val context: Context,
    private val videoDao: VideoDao,
    private val executorService: ExecutorService
) {

    fun getMetadata(video: String, callback: (Search) -> Unit) {
            yasClient.metaById(video).enqueue(object : Callback<Search> {
                override fun onResponse(call: Call<Search>, response: Response<Search>) {
                    if (response.isSuccessful) {
                        response.body()?.let { s -> callback(s) }
                    } else {
                        Log.e(TAG, "Meta Request does not succeed")
                    }
                }

                override fun onFailure(call: Call<Search>, t: Throwable) {
                    Log.e(TAG, "Meta Request failed")
                }
            })
    }

    fun downloadTrack(video: DownloadRequest) {
        yasClient.trackById(video.id).execute().body()?.let { saveToMediaStore(it, video) }
    }

    private fun saveToMediaStore(body: ResponseBody, video: DownloadRequest) {
        // Add a media item that other apps shouldn't see until the item is
// fully written to the media store.
        val resolver = context.contentResolver

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
            val title = "${video.escapedTitle()}.mp3"
            val quoted = "'$title'"
            put(MediaStore.Audio.Media.DISPLAY_NAME, quoted)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
            // TODO affichage de la dur??e marche pas -> voir si elle est bien persist??e
            put(MediaStore.Audio.Media.DURATION, video.duration)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                val directory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                put(MediaStore.Audio.Media.DATA, "${directory}${File.separator}$title")
            }
        }

        var trackUri: Uri? = null
        resolver.insert(audioCollection, songDetails)?.let { songContentUri ->
            trackUri = songContentUri
            resolver.openFileDescriptor(songContentUri, "w", null)?.use { pfd ->
                Log.d(TAG, "File descriptor opened")
                FileOutputStream(pfd.fileDescriptor).use { outputStream ->
                    body.byteStream().use { inputStream ->
                        copyContent(inputStream, outputStream)
                    }
                }
            }
            Log.d(TAG, "Track saved as sound media ")
        } ?: Log.e(TAG, "Failed to saved track")

        // Update status if possible
        trackUri?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                songDetails.clear()
                songDetails.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(trackUri!!, songDetails, null, null)
                Log.d(TAG, "Track marked as ready to use")
            }
        }
    }

    private fun copyContent(inputStream: InputStream, outputStream: FileOutputStream) {
        Log.d(TAG, "Copying content to file stream")
        val buffer = ByteArray(4096)
        var fileSizeDownloaded: Long = 0
        while (true) {
            val read: Int = inputStream.read(buffer)
            if (read == -1) {
                break
            }
            outputStream.write(buffer, 0, read)
            fileSizeDownloaded += read.toLong()
            Log.d("File download: ", "$fileSizeDownloaded")
        }
        outputStream.flush()
    }

    companion object {
        private const val TAG = "RemoteTrackRepository"
    }

}