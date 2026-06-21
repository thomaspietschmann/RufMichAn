package de.pietschie.rufmichan.call.ui

import de.pietschie.rufmichan.data.settings.CallStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InCallActionsTest {

    @Test
    fun everyStyleExposesSixActions() {
        CallStyle.entries.forEach { style ->
            assertEquals("$style should expose 6 actions", 6, style.inCallActions().size)
        }
    }

    @Test
    fun keypadIsPresentInEveryDesign() {
        CallStyle.entries.forEach { style ->
            assertTrue(
                "$style must offer the keypad button",
                style.inCallActions().contains(InCallAction.KEYPAD)
            )
        }
    }

    @Test
    fun toggleableFlagsMatchExpectation() {
        listOf(
            InCallAction.MUTE, InCallAction.SPEAKER, InCallAction.HOLD,
            InCallAction.VIDEO, InCallAction.BLUETOOTH, InCallAction.RECORD,
        ).forEach { assertTrue("$it should be toggleable", it.toggleable) }

        listOf(InCallAction.KEYPAD, InCallAction.ADD_CALL).forEach {
            assertFalse("$it should not be toggleable", it.toggleable)
        }
    }

    @Test
    fun designsHaveDistinctCharacteristicSets() {
        // Samsung leads with "Add call"; MIUI leads with "Record" — the sets are not identical.
        assertEquals(InCallAction.ADD_CALL, CallStyle.SAMSUNG.inCallActions().first())
        assertEquals(InCallAction.RECORD, CallStyle.MIUI.inCallActions().first())
        assertTrue(CallStyle.SAMSUNG.inCallActions() != CallStyle.MIUI.inCallActions())
        assertTrue(CallStyle.SAMSUNG.inCallActions() != CallStyle.SYSTEM.inCallActions())
    }

    @Test
    fun everyActionHasANonZeroLabel() {
        InCallAction.entries.forEach { assertTrue(it.labelRes != 0) }
    }
}
