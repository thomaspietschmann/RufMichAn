package de.pietschie.rufmichan.call.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pietschie.rufmichan.R
import de.pietschie.rufmichan.data.settings.CallStyle

/**
 * A compact, tappable thumbnail of an incoming-call screen rendered in a given [CallStyle], used
 * in Settings so the user can see each design before choosing it. The selected card is outlined
 * in the design's accent colour.
 */
@Composable
fun CallStylePreview(
    style: CallStyle,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = style.toCallTheme()
    val shape = RoundedCornerShape(18.dp)
    val outline =
        if (selected) BorderStroke(2.dp, theme.answerColor)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(124.dp)
    ) {
        Box(
            modifier = Modifier
                .width(124.dp)
                .height(208.dp)
                .clip(shape)
                .border(outline, shape)
                .background(theme.background)
                .clickable(onClick = onClick)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                ContactAvatar(
                    photoPath = null,
                    name = "A",
                    size = 48.dp,
                    backgroundColor = theme.avatarBackground,
                    iconTint = theme.avatarIconTint
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Alex M.",
                    color = theme.nameColor,
                    fontSize = 13.sp,
                    fontWeight = theme.nameFontWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.incoming_call),
                    color = theme.statusColor,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniCallButton(theme.declineColor, theme.buttonShape) {
                        Icon(Icons.Filled.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    MiniCallButton(theme.answerColor, theme.buttonShape) {
                        Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 2
        )
    }
}

@Composable
private fun MiniCallButton(color: Color, shape: Shape, icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(shape)
            .background(color),
        contentAlignment = Alignment.Center
    ) { icon() }
}
