package de.pietschie.rufmichan.call.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    onHangUp: () -> Unit
) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            elapsedSeconds++
        }
    }

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
            Spacer(modifier = Modifier.height(80.dp))

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

            CallActionButton(
                onClick = onHangUp,
                backgroundColor = theme.declineColor,
                contentDescription = stringResource(R.string.hang_up),
                shape = theme.buttonShape,
                size = theme.buttonSize,
                iconSize = theme.buttonIconSize,
                labelColor = theme.labelColor,
                modifier = Modifier.padding(bottom = 80.dp),
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

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
