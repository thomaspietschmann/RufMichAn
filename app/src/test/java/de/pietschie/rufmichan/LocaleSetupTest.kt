package de.pietschie.rufmichan

import androidx.appcompat.app.AppCompatActivity
import de.pietschie.rufmichan.call.CallActivity
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression guard for the per-app language switch: `AppCompatDelegate.setApplicationLocales`
 * only applies on API < 33 when the hosting activities run through AppCompat. If an activity is
 * reverted to a plain ComponentActivity, language changes silently stop working — these checks
 * fail before that ships.
 */
class LocaleSetupTest {

    @Test
    fun mainActivityRunsThroughAppCompat() {
        assertTrue(AppCompatActivity::class.java.isAssignableFrom(MainActivity::class.java))
    }

    @Test
    fun callActivityRunsThroughAppCompat() {
        assertTrue(AppCompatActivity::class.java.isAssignableFrom(CallActivity::class.java))
    }
}
