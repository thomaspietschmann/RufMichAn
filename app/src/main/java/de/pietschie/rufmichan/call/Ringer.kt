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

    // @GuardedBy("lock")
    private val lock = Any()
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var stopped = false

    private val audioManager = context.getSystemService(AudioManager::class.java)

    /** Starts ringing. Safe to call from the main thread — audio I/O runs off-thread. */
    fun start() {
        if (!shouldVibrate() && !shouldRing()) return

        if (shouldVibrate()) startVibration()
        if (shouldRing()) startRingtone()
    }

    fun stop() {
        synchronized(lock) {
            stopped = true
            mediaPlayer?.apply {
                try { if (isPlaying) stop() } catch (_: Exception) {}
                release()
            }
            mediaPlayer = null
        }
        vibrator?.cancel()
        vibrator = null
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private fun shouldRing(): Boolean =
        audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    private fun shouldVibrate(): Boolean =
        audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT

    private fun startRingtone() {
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) ?: return

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

            // Q3: use prepareAsync() so audio I/O never blocks the main thread.
            player.setOnPreparedListener { mp ->
                synchronized(lock) {
                    if (stopped) {
                        // stop() was called before prepare finished — clean up immediately.
                        mp.release()
                        return@setOnPreparedListener
                    }
                    mp.start()
                    mediaPlayer = mp
                }
            }
            player.setOnErrorListener { mp, _, _ ->
                mp.release()
                synchronized(lock) { if (mediaPlayer === mp) mediaPlayer = null }
                true
            }
            player.prepareAsync()
        } catch (_: Exception) {
            // Cannot prepare ringtone — vibration-only fallback already started if applicable.
        }
    }

    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator = vib

        val pattern = longArrayOf(0, 800, 600) // wait 0ms, vibrate 800ms, pause 600ms
        vib.vibrate(VibrationEffect.createWaveform(pattern, 0 /* repeat from index 0 */))
    }
}
