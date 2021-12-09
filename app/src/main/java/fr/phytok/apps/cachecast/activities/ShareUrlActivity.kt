package fr.phytok.apps.cachecast.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.handlers.DownloadService
import fr.phytok.apps.cachecast.model.TrackAppData
import fr.phytok.apps.cachecast.model.toTrack
import fr.phytok.apps.cachecast.yas.RemoteTrackRepository
import javax.inject.Inject


@AndroidEntryPoint
class ShareUrlActivity : AppCompatActivity() {

    private val TAG = "LoadUrlActivity"

    @Inject
    lateinit var remoteTrackRepository: RemoteTrackRepository
    @Inject
    lateinit var notificationSender: NotifSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_url)

        loadTrackData()
    }

    private fun loadTrackData() =
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
    }
}