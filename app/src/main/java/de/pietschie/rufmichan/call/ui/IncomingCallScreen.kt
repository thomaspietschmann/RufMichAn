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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.data.contact.ContactEntity
import java.io.File

@Composable
fun IncomingCallScreen(
    contact: ContactEntity,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1C1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Contact avatar
            ContactAvatar(
                photoPath = contact.photoPath,
                name = contact.name,
                size = 120.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contact name
            Text(
                text = contact.name,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Label
            Text(
                text = contact.phoneNumber.ifBlank { stringResource(R.string.mobile) },
                color = Color(0xFFAAAAAA),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status
            Text(
                text = stringResource(R.string.incoming_call),
                color = Color(0xFF888888),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline
                CallActionButton(
                    onClick = onDecline,
                    backgroundColor = Color(0xFFF44336),
                    contentDescription = stringResource(R.string.decline),
                    icon = { Icon(Icons.Filled.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                )

                // Answer
                CallActionButton(
                    onClick = onAnswer,
                    backgroundColor = Color(0xFF3DDC84),
                    contentDescription = stringResource(R.string.answer),
                    icon = { Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                )
            }
        }
    }
}

@Composable
private fun CallActionButton(
    onClick: () -> Unit,
    backgroundColor: Color,
    contentDescription: String,
    icon: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contentDescription,
            color = Color(0xFFAAAAAA),
            fontSize = 12.sp
        )
    }
}
