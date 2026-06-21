package de.pietschie.rufmichan

import android.app.Application
import androidx.compose.runtime.staticCompositionLocalOf
import de.pietschie.rufmichan.call.CallNotifications

class RufMichAnApp : Application() {

    /** Accessed by Receivers and Services that cannot use Compose DI. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CallNotifications.createChannel(this)
    }
}

/** Composition local so every Composable can reach the DI container without
 *  threading it through every function parameter. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided")
}
