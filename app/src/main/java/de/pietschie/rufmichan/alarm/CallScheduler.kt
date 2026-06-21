package de.pietschie.rufmichan.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import de.pietschie.rufmichan.MainActivity

class CallScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Arms an AlarmManager.setAlarmClock for the given call. The row id is used as the
     *  PendingIntent request code so that individual alarms can be cancelled precisely.
     *  @throws SecurityException if exact-alarm permission has been revoked (API 31/32). */
    fun schedule(callId: Long, triggerAtEpochMillis: Long) {
        // Q10: Guard before scheduling so callers can handle the permission-revoked case.
        if (!ExactAlarmPermission.canScheduleExactAlarms(context)) {
            throw SecurityException("Exact alarm permission not granted")
        }
        val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_CALL_ID, callId)
        }
        val alarmPendingIntent = PendingIntent.getBroadcast(
            context,
            callId.toInt(),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // The show intent is displayed in the system's "next alarm" indicator.
        val showIntent = PendingIntent.getActivity(
            context,
            (callId + 1_000_000L).toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtEpochMillis, showIntent),
            alarmPendingIntent
        )
    }

    /** Cancels the alarm for the given call id. */
    fun cancel(callId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            callId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
