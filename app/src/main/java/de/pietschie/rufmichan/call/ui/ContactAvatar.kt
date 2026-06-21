package de.pietschie.rufmichan.call.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

/** Circular contact avatar: shows the photo if available, falls back to a person icon. */
@Composable
fun ContactAvatar(
    photoPath: String?,
    name: String,
    size: Dp = 56.dp,
    backgroundColor: Color = Color(0xFF3A3C3A),
    iconTint: Color = Color(0xFFAAAAAA)
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = name,
                tint = iconTint,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}
