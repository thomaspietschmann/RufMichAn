package de.pietschie.rufmichan

import android.content.Context
import de.pietschie.rufmichan.alarm.CallScheduler
import de.pietschie.rufmichan.data.AppDatabase
import de.pietschie.rufmichan.data.call.CallRepository
import de.pietschie.rufmichan.data.contact.ContactRepository
import de.pietschie.rufmichan.data.media.PhotoStorage
import de.pietschie.rufmichan.data.settings.SettingsRepository

/** Manual DI container. Holds singletons that survive the Activity lifecycle. */
class AppContainer(context: Context) {

    private val db = AppDatabase.getInstance(context)

    val photoStorage = PhotoStorage(context)
    val callScheduler = CallScheduler(context)
    val settingsRepository = SettingsRepository(context)

    val contactRepository = ContactRepository(db.contactDao(), photoStorage)
    val callRepository = CallRepository(db.scheduledCallDao(), callScheduler)
}
