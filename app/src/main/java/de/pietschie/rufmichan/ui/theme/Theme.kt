package de.pietschie.rufmichan.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    primaryContainer = GreenContainer80,
    onPrimaryContainer = OnGreenContainer,
    secondary = Secondary40,
    secondaryContainer = SecondaryContainer,
    background = Background,
    surface = Surface,
    error = Error
)

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    primaryContainer = GreenContainer20,
    onPrimaryContainer = OnGreenContainer20,
    secondary = Secondary80,
    background = DarkBackground,
    surface = DarkSurface
)

@Composable
fun RufMichAnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is Android 12+ but might use proprietary libs on some ROMs; opt out for
    // better F-Droid compatibility and consistent look.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
