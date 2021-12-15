package fr.phytok.apps.cachecast.yas

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.nio.file.Paths
import javax.inject.Inject

class RemoteTrackRepository @Inject constructor(
    private val yasClient: YasApiClient
    ,
    @ApplicationContext private val context: Context
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

    fun downloadTrack(video: String) {
        yasClient.trackById(video).execute().body()?.let { saveToDisk(it, video) }
    }

    private fun saveToDisk(body: ResponseBody, video: String) {
        Log.d(TAG, "MP3 track received")
//        Log.d(TAG, "MP3 response length ${body.contentLength()}")
//        Log.d(TAG, "MP3 response type ${body.contentType()}")
//        Log.d(TAG, "MP3 response exhausted ${body.source().exhausted()}")

        val cacheDir = context.getExternalFilesDir(null)
        val filePath = arrayOf(cacheDir, "$video.mp3").joinToString(File.separator)
        Log.d(TAG, "Saving to $filePath")

        if (File(filePath).exists()) {
            Log.w(TAG, "File exist !! $filePath")
        }

        // https://learntutorials.net/fr/android/topic/1132/retrofit2
        try {
            body.byteStream().use { inputStream ->
                FileOutputStream(filePath).use { outputStream ->
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
            }
        } catch (e: IOException) {
            Log.w(TAG, "Should handle exception", e)
        }
    }

    companion object {
        private const val TAG = "RemoteTrackRepository"
    }

}