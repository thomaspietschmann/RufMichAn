package de.pietschie.rufmichan.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.pietschie.rufmichan.data.call.CallRepository
import de.pietschie.rufmichan.data.call.CallWithContact
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduledListViewModel(
    private val callRepository: CallRepository
) : ViewModel() {

    val activeCalls: StateFlow<List<CallWithContact>> = callRepository.activeCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cancel(callId: Long) {
        viewModelScope.launch { callRepository.cancel(callId) }
    }

    class Factory(private val repo: CallRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ScheduledListViewModel(repo) as T
        }
    }
}
