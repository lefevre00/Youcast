package fr.phytok.apps.youcast.model

import androidx.core.app.NotificationCompat

import android.app.NotificationManager
import android.R




abstract class MockNotificationData {
    // Notification Standard notification get methods:
    // Standard notification values:
    var contentTitle: String? = null
        protected set
    var contentText: String? = null
        protected set
    var priority = 0
        protected set

    // Channel values (O and above) get methods:
    // Notification channel values (O and above):
    var channelId: String? = null
        protected set
    var channelName: CharSequence? = null
        protected set
    var channelDescription: String? = null
        protected set
    var channelImportance = 0
        protected set
    var isChannelEnableVibrate = false
        protected set
    var channelLockscreenVisibility = 0
        protected set
}

/** Represents data needed for BigPictureStyle Notification.  */
class BigPictureStyleSocialAppData private constructor() : MockNotificationData() {
    // Unique data for this Notification.Style:
    val bigImage: Int
    val bigContentTitle: String
    val summaryText: String
    val possiblePostResponses: Array<CharSequence>
    val participants: ArrayList<String>

    override fun toString(): String {
        return "$contentTitle - $contentText"
    }

    companion object {
        private var sInstance: BigPictureStyleSocialAppData? = null
        val instance: BigPictureStyleSocialAppData?
            get() {
                if (sInstance == null) {
                    sInstance = sync
                }
                return sInstance
            }

        @get:Synchronized
        private val sync: BigPictureStyleSocialAppData?
            private get() {
                if (sInstance == null) {
                    sInstance = BigPictureStyleSocialAppData()
                }
                return sInstance
            }
    }

    init {
        // Standard Notification values:
        // Title/Content for API <16 (4.0 and below) devices.
        contentTitle = "Bob's Post"
        contentText = "[Picture] Like my shot of Earth?"
        priority = NotificationCompat.PRIORITY_HIGH

        // Style notification values:
        bigImage = R.drawable.star_big_off
        bigContentTitle = "Bob's Post"
        summaryText = "Like my shot of Earth?"

        // This would be possible responses based on the contents of the post.
        possiblePostResponses = arrayOf("Yes", "No", "Maybe?")
        participants = ArrayList()
        participants.add("Bob Smith")

        // Notification channel values (for devices targeting 26 and above):
        channelId = "channel_social_1"
        // The user-visible name of the channel.
        channelName = "Sample Social"
        // The user-visible description of the channel.
        channelDescription = "Sample Social Notifications"
        channelImportance = NotificationManager.IMPORTANCE_HIGH
        isChannelEnableVibrate = true
        channelLockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
    }
}