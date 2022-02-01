package fr.phytok.apps.cachecast.activities

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.phytok.apps.cachecast.LocalTrackRepository
import fr.phytok.apps.cachecast.model.TrackAppData
import fr.phytok.apps.cachecast.model.toTrack
import fr.phytok.apps.cachecast.util.NotificationSender
import fr.phytok.apps.cachecast.yas.RemoteTrackRepository
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val remoteTrackRepository: RemoteTrackRepository,
    private val localTrackRepository: LocalTrackRepository,
    private val notificationSender: NotificationSender
): ViewModel() {

    val requestStatus = mutableStateOf("Coucou")
    val askedUrl = mutableStateOf("")

    fun tryToLoadUrl(videoUrl: String, onResult: (LoadResult) -> Unit) {
        askedUrl.value = videoUrl
        requestStatus.value = "Loading metadata..."
        videoUrl
            .split("/")
            .last()
            .let { videoId ->
                if (localTrackRepository.exists(videoId)) {
                    requestStatus.value = "Track already cached !"
                    onResult(LoadResult(inCache = true))
                } else {
                    // WArning of format like https://youtube.com/watch?v=UGYBc4tRK5I&feature=share
                    remoteTrackRepository.getMetadata(videoId) { search ->
                        requestStatus.value = "Download requested..."
                        search.toTrack()?.let { track ->
                            localTrackRepository.save(track)
                            notificationSender.showNotification(track)
                            onResult(LoadResult(trackAppData = track))
                        }
                    }
                }
            }
    }


    companion object {
        const val EXTRA_KEY = "KEY"
        private const val TAG = "ShareViewModel"
    }

}

data class LoadResult(val inCache: Boolean = false, val trackAppData: TrackAppData? = null)
