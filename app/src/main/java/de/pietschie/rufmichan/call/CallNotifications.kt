package de.pietschie.rufmichan.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import de.pietschie.rufmichan.R

object CallNotifications {

    const val CHANNEL_ID = "incoming_call_channel"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_incoming_call),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_incoming_call_desc)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // The service manages audio/vibration; silence the channel itself.
                setSound(null, null)
                enableVibration(false)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** Minimal notification used only to satisfy the FGS 5-second contract on stop/error paths
     *  before the service calls stopForeground(). Never shown to the user for more than a few ms. */
    fun buildPlaceholderNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(context.getString(R.string.incoming_call))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
    }

    fun buildIncomingCallNotification(
        context: Context,
        contactName: String,
        callId: Long
    ): Notification {
        // Full-screen intent: opens CallActivity directly when the screen is locked.
        val callActivityIntent = Intent(context, CallActivity::class.java).apply {
            putExtra(CallActivity.EXTRA_CALL_ID, callId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }
        val fullScreenPi = PendingIntent.getActivity(
            context,
            callId.toInt(),
            callActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decline action handled by the service (no UI needed).
        val declineIntent = Intent(context, CallService::class.java).apply {
            putExtra(CallService.EXTRA_CALL_ID, callId)
            putExtra(CallService.EXTRA_ACTION, CallService.ACTION_DECLINE)
        }
        val declinePi = PendingIntent.getService(
            context,
            (callId + 200).toInt(),
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Answer action opens CallActivity with the answer flag set.
        val answerIntent = Intent(context, CallActivity::class.java).apply {
            putExtra(CallActivity.EXTRA_CALL_ID, callId)
            putExtra(CallActivity.EXTRA_AUTO_ANSWER, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val answerPi = PendingIntent.getActivity(
            context,
            (callId + 100).toInt(),
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle(contactName)
            .setContentText(context.getString(R.string.incoming_call))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPi, true)
            .addAction(R.drawable.ic_call_end, context.getString(R.string.decline), declinePi)
            .addAction(R.drawable.ic_call, context.getString(R.string.answer), answerPi)
            .build()
    }
}
