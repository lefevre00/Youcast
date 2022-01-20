package fr.phytok.apps.cachecast.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.LocalTrackRepository
import fr.phytok.apps.cachecast.model.TrackAppData
import fr.phytok.apps.cachecast.model.toTrack
import fr.phytok.apps.cachecast.services.DownloadService
import fr.phytok.apps.cachecast.services.DownloadService.Companion.ACTION_LOAD
import fr.phytok.apps.cachecast.util.NotificationSender
import fr.phytok.apps.cachecast.yas.RemoteTrackRepository
import javax.inject.Inject


@AndroidEntryPoint
class ShareUrlActivity : AppCompatActivity() {

    @Inject
    lateinit var remoteTrackRepository: RemoteTrackRepository
    @Inject
    lateinit var localTrackRepository: LocalTrackRepository
    @Inject
    lateinit var notificationSender: NotificationSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dont reference layout to leave quickly (no setContentView(layout) )

        // TODO permission: ShareUrlActivity should controle write permission

        when (intent?.action) {
            Intent.ACTION_SEND -> loadTrackData()
            else -> run {
                Log.w(TAG, "Unhandled intent action: ${intent?.action}")
                finish()
            }
        }
    }

    private fun loadTrackData() {
        intent.getStringExtra(Intent.EXTRA_TEXT)
            ?.split("/")
            ?.last()
            ?.let { videoId ->
                remoteTrackRepository.getMetadata(videoId) { search ->
                    search.toTrack()?.let { track ->
                        localTrackRepository.save(track)
                        askForDownload(track)
                        notificationSender.showNotification(track)
                        finish()
                    }
                }
            } ?: run {
                Log.d(TAG, "No extra text received")
                finish()
            }
    }

    private fun askForDownload(track: TrackAppData) {
        Log.i(TAG, "received intent for track ${track.id}")
        Intent(this, DownloadService::class.java).also { newIntent ->
            newIntent.action = ACTION_LOAD
            newIntent.putExtra(EXTRA_KEY, track.id)
            startService(newIntent)
        }
    }

    companion object {
        const val EXTRA_KEY = "KEY"
        private const val TAG = "ShareUrlActivity"
    }
}