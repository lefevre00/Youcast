package fr.phytok.apps.cachecast.yas

import android.util.Log
import fr.phytok.apps.cachecast.BuildConfig
import fr.phytok.apps.cachecast.util.createMapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.Executor
import javax.inject.Inject

class RemoteTrackRepository @Inject constructor(
    private val yasClient: YasApiClient
) {

    fun getMetadata(video: String, callback: (Search) -> Unit) {
            yasClient.metaById(video).enqueue(object : Callback<Search> {
                override fun onResponse(call: Call<Search>, response: Response<Search>) {
                    if (response.isSuccessful) {
                        response.body()?.let { s -> callback(s) }
                    } else {
                        Log.e(TAG, "Request does not succeed")
                    }
                }

                override fun onFailure(call: Call<Search>, t: Throwable) {
                    Log.e(TAG, "Request does not succeed")
                }
            })
    }

    fun downloadTrack(video: String) : ByteArray = yasClient.trackById(video)

    companion object {
        val TAG = "RemoteTrackRepository"
    }

}