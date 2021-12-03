package fr.phytok.apps.youcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


object NotificationUtil {
    private const val channelId = "youtrack.notif.channel.download"
    private const val channelName = "Téléchargement en cours"
    private const val channelDescription = "Liste les pistes en cours de téléchargement"

    fun createNotificationChannel(context: Context): String? {

        // NotificationChannels are required for Notifications on O (API 26) and above.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Initializes NotificationChannel.
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = channelDescription

            // Adds NotificationChannel to system. Attempting to create an existing notification
            // channel with its original values performs no operation, so it's safe to perform the
            // below sequence.
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
            channelId
        } else {
            // Returns null for pre-O (26) devices.
            null
        }
    }
}