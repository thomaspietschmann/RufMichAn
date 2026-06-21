package de.pietschie.rufmichan.call.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.pietschie.rufmichan.data.settings.CallStyle

/**
 * Visual parameters for the incoming-call and in-call screens.
 * Each [CallStyle] maps to one instance via [CallStyle.toCallTheme].
 */
data class CallTheme(
    val background: Color,
    val answerColor: Color,
    val declineColor: Color,
    val nameColor: Color,
    val subtitleColor: Color,
    val statusColor: Color,
    val labelColor: Color,
    val durationColor: Color,
    val avatarBackground: Color,
    val avatarIconTint: Color,
    val buttonShape: Shape,
    val buttonSize: Dp,
    val buttonIconSize: Dp,
    val nameFontSize: TextUnit,
    val nameFontWeight: FontWeight,
    val inCallNameFontSize: TextUnit,
)

fun CallStyle.toCallTheme(): CallTheme = when (this) {

    CallStyle.SYSTEM -> CallTheme(
        background = Color(0xFF1A1C1A),
        answerColor = Color(0xFF3DDC84),
        declineColor = Color(0xFFF44336),
        nameColor = Color.White,
        subtitleColor = Color(0xFFAAAAAA),
        statusColor = Color(0xFF888888),
        labelColor = Color(0xFFAAAAAA),
        durationColor = Color(0xFF3DDC84),
        avatarBackground = Color(0xFF3A3C3A),
        avatarIconTint = Color(0xFFAAAAAA),
        buttonShape = androidx.compose.foundation.shape.CircleShape,
        buttonSize = 72.dp,
        buttonIconSize = 32.dp,
        nameFontSize = 32.sp,
        nameFontWeight = FontWeight.Light,
        inCallNameFontSize = 28.sp,
    )

    CallStyle.PIXEL -> CallTheme(
        // Google Pixel: light surface, Material-green buttons, bold name
        background = Color(0xFF1B1B1F),
        answerColor = Color(0xFF4CAF50),
        declineColor = Color(0xFFE53935),
        nameColor = Color(0xFFE2E2E5),
        subtitleColor = Color(0xFFBBBBBF),
        statusColor = Color(0xFF9A9A9F),
        labelColor = Color(0xFFBBBBBF),
        durationColor = Color(0xFF4CAF50),
        avatarBackground = Color(0xFF2C2C30),
        avatarIconTint = Color(0xFFBBBBBF),
        buttonShape = androidx.compose.foundation.shape.CircleShape,
        buttonSize = 76.dp,
        buttonIconSize = 34.dp,
        nameFontSize = 34.sp,
        nameFontWeight = FontWeight.Medium,
        inCallNameFontSize = 30.sp,
    )

    CallStyle.SAMSUNG -> CallTheme(
        // Samsung One UI: blue-teal accent, large rounded buttons
        background = Color(0xFF1C1C1E),
        answerColor = Color(0xFF1BA1E2),
        declineColor = Color(0xFFE51A1A),
        nameColor = Color.White,
        subtitleColor = Color(0xFFABB2BA),
        statusColor = Color(0xFF7F8C8D),
        labelColor = Color(0xFFABB2BA),
        durationColor = Color(0xFF1BA1E2),
        avatarBackground = Color(0xFF2D3035),
        avatarIconTint = Color(0xFFABB2BA),
        buttonShape = RoundedCornerShape(20.dp),
        buttonSize = 80.dp,
        buttonIconSize = 36.dp,
        nameFontSize = 30.sp,
        nameFontWeight = FontWeight.SemiBold,
        inCallNameFontSize = 26.sp,
    )

    CallStyle.MIUI -> CallTheme(
        // Xiaomi MIUI: warm dark bg, orange accent
        background = Color(0xFF1E1A15),
        answerColor = Color(0xFFFF6900),
        declineColor = Color(0xFFCC3030),
        nameColor = Color(0xFFF5F0EA),
        subtitleColor = Color(0xFFB0A898),
        statusColor = Color(0xFF8A7F75),
        labelColor = Color(0xFFB0A898),
        durationColor = Color(0xFFFF6900),
        avatarBackground = Color(0xFF2E2820),
        avatarIconTint = Color(0xFFB0A898),
        buttonShape = androidx.compose.foundation.shape.CircleShape,
        buttonSize = 72.dp,
        buttonIconSize = 32.dp,
        nameFontSize = 32.sp,
        nameFontWeight = FontWeight.Normal,
        inCallNameFontSize = 28.sp,
    )

    CallStyle.ONEPLUS -> CallTheme(
        // OnePlus: near-black, signature red accent, clean minimal
        background = Color(0xFF0A0A0A),
        answerColor = Color(0xFF1DB954),
        declineColor = Color(0xFFEB0029),
        nameColor = Color.White,
        subtitleColor = Color(0xFF999999),
        statusColor = Color(0xFF666666),
        labelColor = Color(0xFF999999),
        durationColor = Color(0xFF1DB954),
        avatarBackground = Color(0xFF1A1A1A),
        avatarIconTint = Color(0xFF999999),
        buttonShape = RoundedCornerShape(16.dp),
        buttonSize = 74.dp,
        buttonIconSize = 32.dp,
        nameFontSize = 33.sp,
        nameFontWeight = FontWeight.Light,
        inCallNameFontSize = 28.sp,
    )
}
