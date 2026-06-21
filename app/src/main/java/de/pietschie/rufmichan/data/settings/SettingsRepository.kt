package de.pietschie.rufmichan.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Call-screen themes available in Settings. */
enum class CallStyle {
    SYSTEM,   // Current green default
    PIXEL,    // Google Pixel style
    SAMSUNG,  // Samsung One UI style
    MIUI,     // Xiaomi MIUI style
    ONEPLUS   // OnePlus style
}

/** Production DataStore for settings. Injected into [SettingsRepository] so tests can supply
 *  their own (e.g. a temp-file store). */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val CALL_STYLE = stringPreferencesKey("call_style")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    }

    /** The selected call-screen theme. Defaults to SYSTEM. */
    val callStyle: Flow<CallStyle> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.CALL_STYLE] ?: CallStyle.SYSTEM.name
        runCatching { CallStyle.valueOf(raw) }.getOrDefault(CallStyle.SYSTEM)
    }

    /** The selected BCP-47 language tag (e.g. "de", "es"). Empty string = follow OS. */
    val languageTag: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE_TAG] ?: ""
    }

    suspend fun setCallStyle(style: CallStyle) {
        dataStore.edit { prefs -> prefs[Keys.CALL_STYLE] = style.name }
    }

    suspend fun setLanguageTag(tag: String) {
        dataStore.edit { prefs -> prefs[Keys.LANGUAGE_TAG] = tag }
    }
}
