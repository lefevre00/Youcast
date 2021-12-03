package fr.phytok.apps.youcast

import android.app.NotificationManager

import android.app.NotificationChannel
import android.content.Context

import android.os.Build
import fr.phytok.apps.youcast.model.MockNotificationData


object NotificationUtil {
    fun createNotificationChannel(
        context: Context,
        mockNotificationData: MockNotificationData
    ): String? {

        // NotificationChannels are required for Notifications on O (API 26) and above.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // The id of the channel.
            val channelId = mockNotificationData.channelId

            // The user-visible name of the channel.
            val channelName = mockNotificationData.channelName

            // The user-visible description of the channel.
            val channelDescription = mockNotificationData.channelDescription
            val channelImportance = mockNotificationData.channelImportance
            val channelEnableVibrate: Boolean = mockNotificationData.isChannelEnableVibrate
            val channelLockscreenVisibility: Int =
                mockNotificationData.channelLockscreenVisibility

            // Initializes NotificationChannel.
            val notificationChannel = NotificationChannel(channelId, channelName, channelImportance)
            notificationChannel.description = channelDescription
            notificationChannel.enableVibration(channelEnableVibrate)
            notificationChannel.lockscreenVisibility = channelLockscreenVisibility

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