package de.pietschie.rufmichan.call

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.pietschie.rufmichan.RufMichAnApp
import de.pietschie.rufmichan.call.ui.CallTheme
import de.pietschie.rufmichan.call.ui.InCallScreen
import de.pietschie.rufmichan.call.ui.IncomingCallScreen
import de.pietschie.rufmichan.call.ui.toCallTheme
import de.pietschie.rufmichan.data.contact.ContactEntity
import de.pietschie.rufmichan.data.settings.CallStyle
import de.pietschie.rufmichan.ui.theme.RufMichAnTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Full-screen activity that shows the incoming-call or in-call UI.
 * Runs in its own task (singleInstance, excludeFromRecents) and is shown over the lock screen.
 *
 * Lifecycle:
 *  - started by CallService via full-screen intent / FSI notification
 *  - also started by the notification "Answer" action with EXTRA_AUTO_ANSWER=true
 *  - finishes when the user answers + hangs up, or declines
 */
class CallActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var callId: Long = -1L
    private var contact by mutableStateOf<ContactEntity?>(null)
    private var answered by mutableStateOf(false)
    private var callTheme by mutableStateOf<CallTheme>(CallStyle.SYSTEM.toCallTheme())

    private lateinit var proximityController: ProximityController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Show over the lock screen and turn the screen on (API 26 fallback flags).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        callId = intent.getLongExtra(EXTRA_CALL_ID, -1L)
        val autoAnswer = intent.getBooleanExtra(EXTRA_AUTO_ANSWER, false)

        proximityController = ProximityController(this)

        if (callId == -1L) {
            finish()
            return
        }

        // Load contact info and the selected call-screen theme for the UI.
        activityScope.launch {
            val container = (application as RufMichAnApp).container
            val style = container.settingsRepository.callStyle.first()
            callTheme = style.toCallTheme()
            val cwc = container.callRepository.getCallWithContact(callId)
            contact = cwc?.contact
            if (autoAnswer) onAnswer()
        }

        setContent {
            RufMichAnTheme(darkTheme = true) {
                val c = contact
                if (c != null) {
                    if (answered) {
                        InCallScreen(
                            contact = c,
                            theme = callTheme,
                            onHangUp = ::onHangUp
                        )
                    } else {
                        IncomingCallScreen(
                            contact = c,
                            theme = callTheme,
                            onAnswer = ::onAnswer,
                            onDecline = ::onDecline
                        )
                    }
                }
            }
        }
    }

    private fun onAnswer() {
        answered = true
        stopCallService()
        proximityController.acquire()
    }

    private fun onDecline() {
        markCompleted()
        stopCallService()
        finish()
    }

    private fun onHangUp() {
        proximityController.release()
        markCompleted()
        finish()
    }

    private fun stopCallService() {
        val stopIntent = Intent(this, CallService::class.java).apply {
            putExtra(CallService.EXTRA_CALL_ID, callId)
            putExtra(CallService.EXTRA_ACTION, CallService.ACTION_STOP)
        }
        startService(stopIntent)
    }

    private fun markCompleted() {
        activityScope.launch(Dispatchers.IO) {
            (application as RufMichAnApp).container.callRepository.markCompleted(callId)
        }
    }

    // Q8: Handle re-delivery to this singleInstance activity from notification actions
    // (answer/decline tapped while the activity is already alive) or a second call arriving.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val newCallId = intent.getLongExtra(EXTRA_CALL_ID, -1L)
        val autoAnswer = intent.getBooleanExtra(EXTRA_AUTO_ANSWER, false)
        if (newCallId == -1L) return

        callId = newCallId
        answered = false
        contact = null

        activityScope.launch {
            val container = (application as RufMichAnApp).container
            val style = container.settingsRepository.callStyle.first()
            callTheme = style.toCallTheme()
            val cwc = container.callRepository.getCallWithContact(callId)
            contact = cwc?.contact
            if (autoAnswer) onAnswer()
        }
    }

    override fun onDestroy() {
        proximityController.release()
        activityScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_AUTO_ANSWER = "auto_answer"
    }
}
