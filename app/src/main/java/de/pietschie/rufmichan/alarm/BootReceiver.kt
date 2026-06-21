package de.pietschie.rufmichan.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.pietschie.rufmichan.RufMichAnApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // S2: Only handle BOOT_COMPLETED. The app is not Direct-Boot-aware (Room lives in
        // credential-encrypted storage), so LOCKED_BOOT_COMPLETED is deliberately not handled.
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Q6: Use goAsync() so the process stays alive for the duration of the re-arm loop.
        val pending = goAsync()
        val app = context.applicationContext as RufMichAnApp
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
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
                // Clear calls interrupted by the reboot and re-arm any that were waiting.
                app.container.callRepository.recoverInterruptedCalls()
            } finally {
                pending.finish()
            }
        }
    }
}
