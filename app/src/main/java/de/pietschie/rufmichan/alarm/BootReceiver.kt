package de.pietschie.rufmichan.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.pietschie.rufmichan.RufMichAnApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        val app = context.applicationContext as RufMichAnApp
        // Room lives in credential-encrypted storage — it is only available after the user
        // has unlocked the device.  LOCKED_BOOT_COMPLETED fires before unlock, so calls
        // due in that very short window may be missed (documented limitation).
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            try {
                val now = System.currentTimeMillis()
                val calls = app.container.callRepository.getFutureScheduledCalls()
                calls.forEach { cwc ->
                    val call = cwc.call
                    if (call.triggerAtEpochMillis > now) {
                        app.container.callScheduler.schedule(call.id, call.triggerAtEpochMillis)
                    } else {
                        app.container.callRepository.markMissed(call.id)
                    }
                }
            } finally {
                scope.cancel()
            }
        }
    }
}
