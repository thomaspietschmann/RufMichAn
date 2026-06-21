package de.pietschie.rufmichan.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Plays the default ringtone and vibration pattern, respecting the ringer mode and DND. */
class Ringer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val audioManager = context.getSystemService(AudioManager::class.java)

    /** Starts ringing. Must be called from any thread; stops only when [stop] is called. */
    fun start() {
        if (!shouldVibrate() && !shouldRing()) return

        if (shouldVibrate()) {
            startVibration()
        }

        if (shouldRing()) {
            startRingtone()
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private fun shouldRing(): Boolean =
        audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    private fun shouldVibrate(): Boolean =
        audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT

    private fun startRingtone() {
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return

        try {
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setLegacyStreamType(AudioManager.STREAM_RING)
                    .build()
            )
            player.setDataSource(context, ringtoneUri)
            player.isLooping = true
            player.prepare()
            player.start()
            mediaPlayer = player
        } catch (_: Exception) {
            // Cannot play ringtone — vibration-only fallback is already started if applicable.
        }
    }

    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(VibratorManager::class.java))
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator = vib

        val pattern = longArrayOf(0, 800, 600) // wait 0ms, vibrate 800ms, pause 600ms
        vib.vibrate(VibrationEffect.createWaveform(pattern, 0 /* repeat from index 0 */))
    }
}
