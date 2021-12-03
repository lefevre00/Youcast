package fr.phytok.apps.youcast.handlers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationManagerCompat

class CancelTrackIntentService : Service() {

    private lateinit var mNotificationManagerCompat: NotificationManagerCompat

    companion object {
        val ACTION_CANCEL  = "fr.phytok.apps.youcast.action.cancel"
        val EXTRA_NOTIF_ID = "fr.phytok.apps.youcast.extra.notification.id"
    }

    override fun onCreate() {
        mNotificationManagerCompat = NotificationManagerCompat.from(applicationContext);
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("CancelService", "Received command")
        intent
            ?.getIntExtra(EXTRA_NOTIF_ID, 0)
            ?.takeIf { it > 0 }
            ?.let {
                Log.i("CancelService", "ASk to stop notif $it")
                NotificationManagerCompat.from(this).cancel(it)
            } ?: run {
                Log.i("CancelService", "No notif to stop")
            }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}