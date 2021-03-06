package fr.phytok.apps.cachecast.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import fr.phytok.apps.cachecast.activities.ShareUrlActivity
import fr.phytok.apps.cachecast.util.NotificationSender
import fr.phytok.apps.cachecast.yas.RemoteTrackRepository
import javax.inject.Inject

data class DownloadRequest(
    val id: String,
    val title: String,
    val channel: String,
    val duration: Long?,
) {
    fun escapedTitle() = title.replace(" ", "_")
}

fun Bundle.toDownloadRequest(): DownloadRequest = DownloadRequest(
    getString(ShareUrlActivity.EXTRA_ID) ?: "",
    getString(ShareUrlActivity.EXTRA_TITLE) ?: "",
    getString(ShareUrlActivity.EXTRA_CHANNEL) ?: "",
    getLong(ShareUrlActivity.EXTRA_DURATION, 0)
)

@AndroidEntryPoint
class DownloadService : Service() {

    companion object {
        val TAG = "DownloadService"
        private val prefix = "fr.phytok.apps.cachecast"
        val ACTION_CANCEL = "$prefix.action.cancel"
        val ACTION_LOAD = "$prefix.action.load"
        val EXTRA_NOTIF_ID = "$prefix.extra.notification.id"
    }

    lateinit var mNotificationManagerCompat: NotificationManagerCompat
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    @Inject
    lateinit var remoteTrackRepository: RemoteTrackRepository

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {

            val downloadRquest = msg.data.toDownloadRequest()
            if (mNotificationManagerCompat.areNotificationsEnabled()) {
                showNotif(downloadRquest.id)
            } else {
                alertNotifDisabled()
            }

            Log.d(TAG, "Start loading sound track")

            try {
                if (downloadRquest.id.isNotBlank()) {
                    remoteTrackRepository.downloadTrack(downloadRquest)
                    Log.d(TAG, "Loaded sound track")
                }
            } catch (e: InterruptedException) {
                Log.e(TAG, "Download interrupted")
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                Log.e(TAG, "Other exception", e)
                Thread.currentThread().interrupt()
            } finally {
                closeNotification(NotificationSender.NOTIFICATION_ID)
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }

    private fun closeNotification(notifId: Int) {
        Log.i(TAG, "Request to stop notif $notifId")
        NotificationManagerCompat.from(this).cancel(notifId)
    }

    private fun alertNotifDisabled() {
        Log.i(TAG, "Notif disabled")
    }

    private fun showNotif(url: String?) {
        // Launch system notif here
        Log.i(TAG, "Notif enabled")
        Toast.makeText(applicationContext, "Notif $url", Toast.LENGTH_SHORT).show()
    }


    override fun onCreate() {
        super.onCreate()

        mNotificationManagerCompat = NotificationManagerCompat.from(applicationContext)

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int =
        when (intent.action) {
            ACTION_CANCEL -> onCancel(intent)
            ACTION_LOAD -> onUrlShared(intent, startId)
            else -> START_NOT_STICKY
        }

    private fun onUrlShared(intent: Intent, startId: Int): Int {
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            msg.data = Bundle().apply {
                arrayOf(
                    ShareUrlActivity.EXTRA_ID,
                    ShareUrlActivity.EXTRA_TITLE,
                    ShareUrlActivity.EXTRA_CHANNEL,
                    ShareUrlActivity.EXTRA_DURATION
                )
                    .forEach { extra ->
                        intent.getStringExtra(extra)?.let {
                            putString(extra, it)
                        }
                    }
            }
            serviceHandler?.sendMessage(msg)
        }

        // If we get killed, after returning from here, restart
        return START_STICKY
    }

    private fun onCancel(intent: Intent): Int {
        Log.i(TAG, "Received command")
        intent
            .getIntExtra(EXTRA_NOTIF_ID, 0)
            .takeIf { it > 0 }
            ?.let {
                closeNotification(it)
            } ?: run {
            Log.i(TAG, "No notif to stop")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }
}
