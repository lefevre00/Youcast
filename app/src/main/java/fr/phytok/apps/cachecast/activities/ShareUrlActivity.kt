package fr.phytok.apps.cachecast.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.services.DownloadService
import fr.phytok.apps.cachecast.model.TrackAppData
import fr.phytok.apps.cachecast.model.toTrack
import fr.phytok.apps.cachecast.util.NotificationSender
import fr.phytok.apps.cachecast.yas.RemoteTrackRepository
import javax.inject.Inject


@AndroidEntryPoint
class ShareUrlActivity : AppCompatActivity() {

    @Inject
    lateinit var remoteTrackRepository: RemoteTrackRepository
    @Inject
    lateinit var notificationSender: NotificationSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dont reference layout to leave quickly
//        setContentView(R.layout.activity_load_url)

        loadTrackData()
    }

    private fun loadTrackData() =
        // TODO use
//          Intent.ACTION_SEND &&
//          Intent.EXTRA_TEXT
        intent?.clipData
                ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.text?.split("/")?.last()
            ?.let { videoId ->
                remoteTrackRepository.getMetadata(videoId) { search ->
                    search.toTrack()?.let { track ->
                        launchDownload(track)
                        notificationSender.showNotification(track)
                        // Close activity
                        finish()
                    }
                }
            }

    private fun launchDownload(track: TrackAppData) {
        Log.i(TAG, "received intent for track ${track.id}")
        Intent(this, DownloadService::class.java).also { newIntent ->
            newIntent.putExtra(EXTRA_KEY, track.id)
            startService(newIntent)
        }
    }

    companion object {
        const val EXTRA_KEY = "KEY"
        private const val TAG = "ShareUrlActivity"
    }
}