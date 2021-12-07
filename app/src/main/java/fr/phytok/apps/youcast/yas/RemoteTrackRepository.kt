package fr.phytok.apps.youcast.yas

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import java.util.concurrent.Executor
import javax.inject.Inject

class RemoteTrackRepository @Inject constructor(
    private val executor: Executor,
    @ApplicationContext private val context: Context
) {

    var yasClient: YasApiClient = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .build().create(YasApiClient::class.java)

    fun getMetadata(video: String, callback: (Search) -> Unit) =
        executor.execute {
            callback(yasClient.metaById(video))
        }

    fun downloadTrack(video: String) : ByteArray = yasClient.trackById(video)

}