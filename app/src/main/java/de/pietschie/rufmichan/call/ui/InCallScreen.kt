package de.pietschie.rufmichan.call.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.data.contact.ContactEntity
import de.pietschie.rufmichan.data.settings.CallStyle
import kotlinx.coroutines.delay

@Composable
fun InCallScreen(
    contact: ContactEntity,
    theme: CallTheme = CallStyle.SYSTEM.toCallTheme(),
    style: CallStyle = CallStyle.SYSTEM,
    onHangUp: () -> Unit
) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            elapsedSeconds++
        }
    }

    // Visual-only toggle state for actions like mute/speaker — no real telephony effect.
    val toggled = remember { mutableStateMapOf<InCallAction, Boolean>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            ContactAvatar(
                photoPath = contact.photoPath,
                name = contact.name,
                size = 100.dp,
                backgroundColor = theme.avatarBackground,
                iconTint = theme.avatarIconTint
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = contact.name,
                color = theme.nameColor,
                fontSize = theme.inCallNameFontSize,
                fontWeight = theme.nameFontWeight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatDuration(elapsedSeconds),
                color = theme.durationColor,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Secondary actions, grouped in rows of three, styled per design.
            style.inCallActions().chunked(3).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    rowActions.forEach { action ->
                        InCallActionButton(
                            action = action,
                            active = toggled[action] == true,
                            theme = theme,
                            onClick = {
                                if (action.toggleable) {
                                    toggled[action] = !(toggled[action] ?: false)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            CallActionButton(
                onClick = onHangUp,
                backgroundColor = theme.declineColor,
                contentDescription = stringResource(R.string.hang_up),
                shape = theme.buttonShape,
                size = theme.buttonSize,
                iconSize = theme.buttonIconSize,
                labelColor = theme.labelColor,
                modifier = Modifier.padding(bottom = 48.dp),
                icon = {
                    Icon(
                        Icons.Filled.CallEnd,
                        contentDescription = stringResource(R.string.hang_up),
                        tint = Color.White,
                        modifier = Modifier.size(theme.buttonIconSize)
                    )
                }
            )
        }
    }
}

/** A single secondary in-call action: a smaller themed button with a label. Toggleable actions
 *  swap their background/icon to an "active" look when [active]. Presentation only. */
@Composable
private fun InCallActionButton(
    action: InCallAction,
    active: Boolean,
    theme: CallTheme,
    onClick: () -> Unit
) {
    val label = stringResource(action.labelRes)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(88.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(60.dp)
                .clip(theme.buttonShape)
                .background(if (active) theme.secondaryButtonActiveColor else theme.secondaryButtonColor)
        ) {
            Icon(
                imageVector = if (active) action.activeIcon else action.icon,
                contentDescription = label,
                tint = if (active) theme.secondaryIconActiveTint else theme.secondaryIconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = theme.labelColor,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
