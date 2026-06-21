package de.pietschie.rufmichan.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.pietschie.rufmichan.data.settings.CallStyle
import de.pietschie.rufmichan.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    val callStyle: StateFlow<CallStyle> = settingsRepository.callStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CallStyle.SYSTEM)

    val languageTag: StateFlow<String> = settingsRepository.languageTag
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun setCallStyle(style: CallStyle) {
        viewModelScope.launch { settingsRepository.setCallStyle(style) }
    }

    fun setLanguageTag(tag: String) {
        viewModelScope.launch { settingsRepository.setLanguageTag(tag) }
    }

    class Factory(private val repo: SettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repo) as T
        }
    }
}
