package de.pietschie.rufmichan.call.ui

import de.pietschie.rufmichan.data.settings.CallStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CallThemeTest {

    @Test
    fun everyStyleMapsToATheme() {
        // Should not throw and must cover the whole enum.
        CallStyle.entries.forEach { it.toCallTheme() }
    }

    @Test
    fun accentColoursAreDistinctPerDesign() {
        val accents = CallStyle.entries.map { it.toCallTheme().answerColor }.toSet()
        assertEquals("Each design should have its own accent colour", CallStyle.entries.size, accents.size)
    }

    @Test
    fun secondaryButtonStylingIsDefined() {
        CallStyle.entries.forEach { style ->
            val theme = style.toCallTheme()
            // Active state must visually differ from the resting state.
            assertNotEquals(
                "$style secondary active colour should differ from the resting colour",
                theme.secondaryButtonColor,
                theme.secondaryButtonActiveColor
            )
        }
    }
}
