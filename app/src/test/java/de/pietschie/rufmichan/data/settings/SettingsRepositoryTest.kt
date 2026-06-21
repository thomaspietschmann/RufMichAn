package de.pietschie.rufmichan.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repo: SettingsRepository

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStore = PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            File(tmp.newFolder(), "settings.preferences_pb")
        }
        repo = SettingsRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun defaultsAreSystemStyleAndFollowOsLanguage() = runTest {
        assertEquals(CallStyle.SYSTEM, repo.callStyle.first())
        assertEquals("", repo.languageTag.first())
    }

    @Test
    fun callStyleRoundTrips() = runTest {
        repo.setCallStyle(CallStyle.SAMSUNG)
        assertEquals(CallStyle.SAMSUNG, repo.callStyle.first())

        repo.setCallStyle(CallStyle.ONEPLUS)
        assertEquals(CallStyle.ONEPLUS, repo.callStyle.first())
    }

    @Test
    fun languageTagRoundTrips() = runTest {
        repo.setLanguageTag("fr")
        assertEquals("fr", repo.languageTag.first())

        repo.setLanguageTag("")
        assertEquals("", repo.languageTag.first())
    }
}
