package de.pietschie.rufmichan.call.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.data.settings.CallStyle

/**
 * Secondary in-call actions shown as a grid above the hang-up button. These are presentation
 * only — they don't perform any real telephony function; [toggleable] entries just flip their
 * visual highlight when tapped. [activeIcon] is used while a toggleable action is "on".
 */
enum class InCallAction(
    val labelRes: Int,
    val icon: ImageVector,
    val activeIcon: ImageVector = icon,
    val toggleable: Boolean = false,
) {
    MUTE(R.string.action_mute, Icons.Filled.Mic, Icons.Filled.MicOff, toggleable = true),
    KEYPAD(R.string.action_keypad, Icons.Filled.Dialpad),
    SPEAKER(R.string.action_speaker, Icons.AutoMirrored.Filled.VolumeUp, toggleable = true),
    ADD_CALL(R.string.action_add_call, Icons.Filled.PersonAdd),
    HOLD(R.string.action_hold, Icons.Filled.Pause, toggleable = true),
    VIDEO(R.string.action_video, Icons.Filled.Videocam, toggleable = true),
    BLUETOOTH(R.string.action_bluetooth, Icons.Filled.Bluetooth, toggleable = true),
    RECORD(R.string.action_record, Icons.Filled.FiberManualRecord, toggleable = true),
}

/**
 * The characteristic set of in-call buttons each OEM design shows. Every set keeps [KEYPAD]
 * present, mirroring real dialer UIs. The order matches the on-device layout it imitates.
 */
fun CallStyle.inCallActions(): List<InCallAction> = when (this) {
    CallStyle.SYSTEM, CallStyle.PIXEL -> listOf(
        InCallAction.MUTE, InCallAction.KEYPAD, InCallAction.SPEAKER,
        InCallAction.ADD_CALL, InCallAction.HOLD, InCallAction.VIDEO,
    )
    CallStyle.SAMSUNG -> listOf(
        InCallAction.ADD_CALL, InCallAction.VIDEO, InCallAction.BLUETOOTH,
        InCallAction.SPEAKER, InCallAction.MUTE, InCallAction.KEYPAD,
    )
    CallStyle.MIUI -> listOf(
        InCallAction.RECORD, InCallAction.SPEAKER, InCallAction.MUTE,
        InCallAction.KEYPAD, InCallAction.ADD_CALL, InCallAction.VIDEO,
    )
    CallStyle.ONEPLUS -> listOf(
        InCallAction.MUTE, InCallAction.KEYPAD, InCallAction.SPEAKER,
        InCallAction.ADD_CALL, InCallAction.RECORD, InCallAction.VIDEO,
    )
}
