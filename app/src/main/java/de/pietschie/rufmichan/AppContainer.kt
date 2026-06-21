package de.pietschie.rufmichan

import android.content.Context
import de.pietschie.rufmichan.alarm.CallScheduler
import de.pietschie.rufmichan.data.AppDatabase
import de.pietschie.rufmichan.data.call.CallRepository
import de.pietschie.rufmichan.data.contact.ContactRepository
import de.pietschie.rufmichan.data.media.PhotoStorage
import de.pietschie.rufmichan.data.settings.SettingsRepository
import de.pietschie.rufmichan.data.settings.settingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual DI container. Holds singletons that survive the Activity lifecycle. */
class AppContainer(context: Context) {

    private val db = AppDatabase.getInstance(context)

    /** Process-lifetime scope for work that must outlive a Service/Receiver (e.g. completing
     *  a call declined from the notification, which then stops the service). */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val photoStorage = PhotoStorage(context)
    val callScheduler = CallScheduler(context)
    val settingsRepository = SettingsRepository(context.settingsDataStore)

    val contactRepository = ContactRepository(db.contactDao(), photoStorage)
    val callRepository = CallRepository(db.scheduledCallDao(), callScheduler)
}
