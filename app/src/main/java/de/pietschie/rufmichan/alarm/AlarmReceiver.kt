package de.pietschie.rufmichan.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import de.pietschie.rufmichan.call.CallService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getLongExtra(EXTRA_CALL_ID, -1L)
        if (callId == -1L) return

        val serviceIntent = Intent(context, CallService::class.java).apply {
            putExtra(CallService.EXTRA_CALL_ID, callId)
        }
        // Starting a foreground service from a broadcast receiver is a documented exemption
        // for apps that hold the SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM permission.
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_CALL_ID = "call_id"
    }
}
