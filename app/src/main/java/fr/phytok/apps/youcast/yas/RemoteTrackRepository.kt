package fr.phytok.apps.youcast.yas

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.phytok.apps.youcast.R
import fr.phytok.apps.youcast.util.createMapper
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.Executor
import javax.inject.Inject

class RemoteTrackRepository @Inject constructor(
    private val executor: Executor,
    @ApplicationContext private val context: Context
) {

    val TAG = "RemoteTrackRepository"

    var yasClient: YasApiClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.server_url))
        .addConverterFactory(JacksonConverterFactory.create(createMapper()))
        .build().create(YasApiClient::class.java)

    fun getMetadata(video: String, callback: (Search) -> Unit) =

        executor.execute {
            val response = yasClient.metaById(video).execute()
            if (response.isSuccessful) {
                response.body()?.let { s -> callback(s) }
            } else {
                Log.e(TAG, "Request does not succeed")
            }
        }

    fun downloadTrack(video: String) : ByteArray = yasClient.trackById(video)

}