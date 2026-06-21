package de.pietschie.rufmichan.call

import android.app.Service
import android.content.Intent
import android.os.IBinder
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
    private var currentCallId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        ringer = Ringer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callId = intent?.getLongExtra(EXTRA_CALL_ID, -1L) ?: -1L
        val action = intent?.getStringExtra(EXTRA_ACTION)

        if (action == ACTION_DECLINE || action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (callId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        currentCallId = callId

        // Post the notification as quickly as possible to satisfy foreground-service rules.
        val repo = (application as RufMichAnApp).container.callRepository
        serviceScope.launch {
            val callWithContact = repo.getCallWithContact(callId)
            val contactName = callWithContact?.contact?.name ?: "Unknown"

            val notification = CallNotifications.buildIncomingCallNotification(
                this@CallService, contactName, callId
            )
            startForeground(CallNotifications.NOTIFICATION_ID, notification)

            repo.markFired(callId)
        }

        ringer?.start()

        return START_NOT_STICKY
    }

    /** Called by CallActivity when the user answers or declines. */
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
