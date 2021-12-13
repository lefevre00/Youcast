package fr.phytok.apps.cachecast.activities

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.phytok.apps.cachecast.GlobalNotificationBuilder
import fr.phytok.apps.cachecast.NotificationUtil
import fr.phytok.apps.cachecast.R
import fr.phytok.apps.cachecast.services.CancelTrackIntentService
import fr.phytok.apps.cachecast.model.TrackAppData
import javax.inject.Inject

class NotificationSender @Inject constructor(
    @ApplicationContext private val context: Context
){

    private val mNotificationManagerCompat = NotificationManagerCompat.from(context)

    fun showNotification(track: TrackAppData) {

        Log.d(TAG, "In notifyUrlReceived")

        // Main steps for building a BIG_PICTURE_STYLE notification:
        //      0. Get your data
        //      1. Create/Retrieve Notification Channel for O and beyond devices (26+)
        //      2. Build the BIG_PICTURE_STYLE
        //      3. Set up main Intent for notification
        //      4. Set up RemoteInput, so users can input (keyboard and voice) from notification
        //      5. Build and issue the notification

        // 1. Create/Retrieve Notification Channel for O and beyond devices (26+).
        val notificationChannelId = NotificationUtil.createNotificationChannel(context)

        // 2. Build the BIG_PICTURE_STYLE.
//        val bigPictureStyle: BigPictureStyle =
//            NotificationCompat.BigPictureStyle() // Provides the bitmap for the BigPicture notification.
//                .bigPicture(
//                    BitmapFactory.decodeResource(
//                        resources,
//                        bigPictureStyleSocialAppData.getBigImage()
//                    )
//                ) // Overrides ContentTitle in the big form of the template.
//                .setBigContentTitle(bigPictureStyleSocialAppData.getBigContentTitle()) // Summary line after the detail section in the big form of the template.
//                .setSummaryText(bigPictureStyleSocialAppData.getSummaryText())

        // 3. Set up main Intent for notification.
        val mainIntent = Intent(context, DownloadStatusActivity::class.java)

        // When creating your Intent, you need to take into account the back state, i.e., what
        // happens after your Activity launches and the user presses the back button.

        // There are two options:
        //      1. Regular activity - You're starting an Activity that's part of the application's
        //      normal workflow.

        //      2. Special activity - The user only sees this Activity if it's started from a
        //      notification. In a sense, the Activity extends the notification by providing
        //      information that would be hard to display in the notification itself.

        // Even though this sample's MainActivity doesn't link to the Activity this Notification
        // launches directly, i.e., it isn't part of the normal workflow, a social app generally
        // always links to individual posts as part of the app flow, so we will follow option 1.

        // For an example of option 2, check out the BIG_TEXT_STYLE example.

        // For more information, check out our dev article:
        // https://developer.android.com/training/notify-user/navigation.html

        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack.
        stackBuilder.addParentStack(DownloadStatusActivity::class.java)
        // Adds the Intent to the top of the stack.
        stackBuilder.addNextIntent(mainIntent)
        // Gets a PendingIntent containing the entire back stack.
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mainIntent.putExtra(CancelTrackIntentService.EXTRA_NOTIF_ID, Companion.NOTIFICATION_ID)

        // 4. Set up RemoteInput, so users can input (keyboard and voice) from notification.

        // Note: For API <24 (M and below) we need to use an Activity, so the lock-screen presents
        // the auth challenge. For API 24+ (N and above), we use a Service (could be a
        // BroadcastReceiver), so the user can input from Notification or lock-screen (they have
        // choice to allow) without leaving the notification.

        // Create the RemoteInput.
        val cancelLabel = context.getString(R.string.cancel)
//        val remoteInput = RemoteInput.Builder(CancelTrackIntentService.EXTRA_CANCEL)
//            .setLabel(cancelLabel) // List of quick response choices for any wearables paired with the phone
//            .setChoices(appData.possiblePostResponses)
//            .build()

        // Pending intent =
        //      API <24 (M and below): activity so the lock-screen presents the auth challenge
        //      API 24+ (N and above): this should be a Service or BroadcastReceiver
        val cancelActionPendingIntent: PendingIntent

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent(context, CancelTrackIntentService::class.java)
            intent.action = CancelTrackIntentService.ACTION_CANCEL
            intent.putExtra(CancelTrackIntentService.EXTRA_NOTIF_ID, Companion.NOTIFICATION_ID)
            cancelActionPendingIntent = PendingIntent.getService(context, 0, intent, 0)
        } else {
            cancelActionPendingIntent = mainPendingIntent
        }

        val cancelAction: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.drawable.ic_cancel_18dp,
            cancelLabel,
            cancelActionPendingIntent
        )
//            .addRemoteInput(remoteInput)
            .build()

        // 5. Build and issue the notification.

        // Because we want this to be a new notification (not updating a previous notification), we
        // create a new Builder. Later, we use the same global builder to get back the notification
        // we built here for a comment on the post.
        val notificationCompatBuilder = NotificationCompat.Builder(
            context, notificationChannelId ?: ""
        )

        GlobalNotificationBuilder.notificationCompatBuilderInstance = notificationCompatBuilder

        notificationCompatBuilder // BIG_PICTURE_STYLE sets title and content for API 16 (4.1 and after).
//            .setStyle(bigPictureStyle) // Title for API <16 (4.0 and below) devices.
            .setContentTitle(track.title) // Content for API <24 (7.0 and below) devices.
//            .setContentText(getString(R.string.downloading))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(
                Picasso.get()
                    .load(track.picture?.url)
                    .resize(60, 60)
                    .centerCrop()
                    .get()
            )
            .setContentIntent(mainPendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Set primary color (important for Wear 2.0 Notifications).
            .setColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimary
                )
            )
            // SIDE NOTE: Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
            // devices and all Wear devices. If you have more than one notification and
            // you prefer a different summary notification, set a group key and create a
            // summary notification via
            // .setGroupSummary(true)
            // .setGroup(GROUP_KEY_YOUR_NAME_HERE)
            .setSubText(1.toString())
            .setProgress(100, 0, true)
            .addAction(cancelAction)
            .setCategory(Notification.CATEGORY_SOCIAL)
        // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
        // 'importance' which is set in the NotificationChannel. The integers representing
        // 'priority' are different from 'importance', so make sure you don't mix them.
//            .setPriority(appData.priority) // Sets lock-screen visibility for 25 and below. For 26 and above, lock screen
        // visibility is set in the NotificationChannel.
//            .setVisibility(appData.channelLockscreenVisibility)

        val notification = notificationCompatBuilder.build()

        mNotificationManagerCompat.notify(Companion.NOTIFICATION_ID, notification)
    }

    companion object {
        private val TAG = "NotifSender"

        // TODO: Should pair Notif ID with track
        val NOTIFICATION_ID = 888
    }

}