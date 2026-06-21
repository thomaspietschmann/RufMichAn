package de.pietschie.rufmichan.call

import android.content.Context
import android.os.PowerManager

/** Manages the PROXIMITY_SCREEN_OFF_WAKE_LOCK so the screen turns off when held to the ear
 *  during an in-call session (exactly like the system Phone app). */
class ProximityController(context: Context) {

    private val powerManager = context.getSystemService(PowerManager::class.java)
    private var wakeLock: PowerManager.WakeLock? = null

    val isSupported: Boolean
        get() = powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)

    fun acquire() {
        if (!isSupported || wakeLock?.isHeld == true) return
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "RufMichAn:proximity"
        )
        wakeLock?.acquire()
    }

    fun release() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }
}
