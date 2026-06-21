package de.pietschie.rufmichan.call

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.RufMichAnApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that owns the incoming-call notification, ringtone and vibration.
 *
 * It is started by AlarmReceiver when an alarm fires, and stopped either when the user
 * answers/declines via CallActivity, when they tap "Decline" in the notification action,
 * or when the auto-timeout expires.
 */
class CallService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var ringer: Ringer? = null

    override fun onCreate() {
        super.onCreate()
        ringer = Ringer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callId = intent?.getLongExtra(EXTRA_CALL_ID, -1L) ?: -1L
        val action = intent?.getStringExtra(EXTRA_ACTION)

        // Q1+Q2: Always call startForeground() FIRST, synchronously, to satisfy the
        // 5-second FGS contract on every code path (including stop/decline paths).
        if (action == ACTION_DECLINE || action == ACTION_STOP || callId == -1L) {
            // Stop paths: post a silent placeholder and immediately remove it.
            startForeground(
                CallNotifications.NOTIFICATION_ID,
                CallNotifications.buildPlaceholderNotification(this)
            )
            @Suppress("DEPRECATION")
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        // Normal call-start path: post the notification with FSI immediately using a
        // generic title, then update it with the real contact name asynchronously.
        startForeground(
            CallNotifications.NOTIFICATION_ID,
            CallNotifications.buildIncomingCallNotification(
                this, getString(R.string.incoming_call), callId
            )
        )

        val repo = (application as RufMichAnApp).container.callRepository
        serviceScope.launch {
            val callWithContact = repo.getCallWithContact(callId)
            val contactName = callWithContact?.contact?.name
                ?: getString(R.string.unknown_caller)

            // Update notification text with the real contact name.
            val notification = CallNotifications.buildIncomingCallNotification(
                this@CallService, contactName, callId
            )
            getSystemService(NotificationManager::class.java)
                ?.notify(CallNotifications.NOTIFICATION_ID, notification)

            repo.markFired(callId)
        }

        ringer?.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ringer?.stop()
        ringer = null
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_ACTION = "action"
        const val ACTION_DECLINE = "decline"
        const val ACTION_STOP = "stop"
    }
}
