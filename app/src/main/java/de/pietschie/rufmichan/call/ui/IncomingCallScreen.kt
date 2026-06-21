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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.data.contact.ContactEntity
import de.pietschie.rufmichan.data.settings.CallStyle

@Composable
fun IncomingCallScreen(
    contact: ContactEntity,
    theme: CallTheme = CallStyle.SYSTEM.toCallTheme(),
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
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
                size = 120.dp,
                backgroundColor = theme.avatarBackground,
                iconTint = theme.avatarIconTint
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = contact.name,
                color = theme.nameColor,
                fontSize = theme.nameFontSize,
                fontWeight = theme.nameFontWeight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = contact.phoneNumber.ifBlank { stringResource(R.string.mobile) },
                color = theme.subtitleColor,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.incoming_call),
                color = theme.statusColor,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallActionButton(
                    onClick = onDecline,
                    backgroundColor = theme.declineColor,
                    contentDescription = stringResource(R.string.decline),
                    shape = theme.buttonShape,
                    size = theme.buttonSize,
                    iconSize = theme.buttonIconSize,
                    labelColor = theme.labelColor,
                    icon = {
                        Icon(
                            Icons.Filled.CallEnd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(theme.buttonIconSize)
                        )
                    }
                )

                CallActionButton(
                    onClick = onAnswer,
                    backgroundColor = theme.answerColor,
                    contentDescription = stringResource(R.string.answer),
                    shape = theme.buttonShape,
                    size = theme.buttonSize,
                    iconSize = theme.buttonIconSize,
                    labelColor = theme.labelColor,
                    icon = {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(theme.buttonIconSize)
                        )
                    }
                )
            }
        }
    }
}

@Composable
internal fun CallActionButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    contentDescription: String,
    shape: androidx.compose.ui.graphics.Shape,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    labelColor: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(backgroundColor)
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contentDescription,
            color = labelColor,
            fontSize = 12.sp
        )
    }
}
