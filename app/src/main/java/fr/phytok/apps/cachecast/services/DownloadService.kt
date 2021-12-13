package fr.phytok.apps.cachecast.services

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import fr.phytok.apps.cachecast.activities.ShareUrlActivity


open class DownloadService : Service() {

    companion object {
        val TAG = "DownloadService"
        private val prefix = "fr.phytok.apps.cachecast"
        val ACTION_CANCEL  = "$prefix.action.cancel"
        val EXTRA_NOTIF_ID = "$prefix.extra.notification.id"
    }

    lateinit var mNotificationManagerCompat : NotificationManagerCompat
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {

            val url = msg.data.getString(ShareUrlActivity.EXTRA_KEY)
            if (mNotificationManagerCompat.areNotificationsEnabled()) {
                showNotif(url)
            } else {
                alertNotifDisabled(url)
            }

            Log.i(TAG, "Do long stuff")
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1)
        }
    }

    private fun alertNotifDisabled(url: String?) {
        Log.i(TAG,"Notif disabled")
    }

    private fun showNotif(url: String?) {
        Log.i(TAG,"Notif enabled")
        Toast.makeText(applicationContext, "Notif $url", Toast.LENGTH_SHORT).show()
    }


    override fun onCreate() {

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
        when(intent?.action) {
            null -> START_NOT_STICKY
            ACTION_CANCEL -> onCancel(intent)
            Intent.ACTION_SEND -> onUrlShared(intent, startId)
            else -> START_NOT_STICKY
        }

    private fun onUrlShared(intent: Intent, startId: Int): Int {
        Toast.makeText(this, "Service notified", Toast.LENGTH_SHORT).show()

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            msg.data = Bundle().apply {
                putString(ShareUrlActivity.EXTRA_KEY, intent.getStringExtra(ShareUrlActivity.EXTRA_KEY))
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
                Log.i(TAG, "ASk to stop notif $it")
                NotificationManagerCompat.from(this).cancel(it)
            } ?: run {
            Log.i(TAG, "No notif to stop")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }
}
