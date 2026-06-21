package de.pietschie.rufmichan.call

import android.app.Notification
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

    // The call this service instance is currently presenting (-1 = none yet) and its
    // posted notification. Used to keep an in-progress call untouched when a second
    // call's alarm reaches this same running instance.
    private var activeCallId: Long = -1L
    private var activeNotification: Notification? = null

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
            // Decline from the notification ends the call without any UI, so complete it
            // (and promote a waiting call) here. ACTION_STOP comes from CallActivity, which
            // already completes the call itself when appropriate. Use the application scope
            // because stopSelf() cancels this service's own scope.
            if (action == ACTION_DECLINE && callId != -1L) {
                val container = (application as RufMichAnApp).container
                container.applicationScope.launch {
                    container.callRepository.completeAndPromoteWaiting(callId)
                }
            }
            stopSelf()
            return START_NOT_STICKY
        }

        // A different call is already ringing on THIS instance: defer the new one and keep
        // the current call's notification, ringer and full-screen UI completely untouched.
        if (activeCallId != -1L && activeCallId != callId) {
            startForeground(
                CallNotifications.NOTIFICATION_ID,
                activeNotification ?: CallNotifications.buildPlaceholderNotification(this)
            )
            val repo = (application as RufMichAnApp).container.callRepository
            serviceScope.launch { repo.markWaiting(callId) }
            return START_NOT_STICKY
        }

        // Fresh instance: a call may still be active (answered/in-call) even though no
        // service is running for it, so post a silent placeholder and decide once we have
        // queried the DB. Only then do we post the FSI notification and start ringing.
        startForeground(
            CallNotifications.NOTIFICATION_ID,
            CallNotifications.buildPlaceholderNotification(this)
        )

        val repo = (application as RufMichAnApp).container.callRepository
        serviceScope.launch {
            // Defer if another call is in progress, or if this instance was meanwhile claimed
            // for a different call (two alarms firing within the same instant).
            val instanceBusy = activeCallId != -1L && activeCallId != callId
            if (instanceBusy || repo.getActiveCallIdExcluding(callId) != null) {
                repo.markWaiting(callId)
                if (instanceBusy) {
                    // Keep this instance alive for the call it is already presenting.
                    activeNotification?.let {
                        startForeground(CallNotifications.NOTIFICATION_ID, it)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                    stopSelf()
                }
                return@launch
            }

            val callWithContact = repo.getCallWithContact(callId)
            val contactName = callWithContact?.contact?.name
                ?: getString(R.string.unknown_caller)

            // Post the real notification (carries the full-screen intent → CallActivity).
            val notification = CallNotifications.buildIncomingCallNotification(
                this@CallService, contactName, callId
            )
            activeNotification = notification
            startForeground(CallNotifications.NOTIFICATION_ID, notification)
            activeCallId = callId

            repo.markFired(callId)
            ringer?.start()
        }

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
