package de.pietschie.rufmichan.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.pietschie.rufmichan.data.call.CallRepository
import de.pietschie.rufmichan.data.contact.ContactEntity
import de.pietschie.rufmichan.data.contact.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ScheduleMode { COUNTDOWN_SECONDS, COUNTDOWN_MINUTES, TARGET_TIME }

class ScheduleCallViewModel(
    private val contactRepository: ContactRepository,
    private val callRepository: CallRepository,
    private val preselectedContactId: Long?
) : ViewModel() {

    val contacts: StateFlow<List<ContactEntity>> = contactRepository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedContact = MutableStateFlow<ContactEntity?>(null)
    val selectedContact: StateFlow<ContactEntity?> = _selectedContact.asStateFlow()

    private val _mode = MutableStateFlow(ScheduleMode.COUNTDOWN_SECONDS)
    val mode: StateFlow<ScheduleMode> = _mode.asStateFlow()

    private val _countdownSeconds = MutableStateFlow(10)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    private val _countdownMinutes = MutableStateFlow(5)
    val countdownMinutes: StateFlow<Int> = _countdownMinutes.asStateFlow()

    private val _targetHour = MutableStateFlow(12)
    val targetHour: StateFlow<Int> = _targetHour.asStateFlow()

    private val _targetMinute = MutableStateFlow(0)
    val targetMinute: StateFlow<Int> = _targetMinute.asStateFlow()

    /** Set to true after a successful schedule so the screen can navigate away. */
    private val _done = MutableStateFlow(false)
    val done: StateFlow<Boolean> = _done.asStateFlow()

    /** True when the target time was in the past and rolled to tomorrow. */
    private val _rolledToTomorrow = MutableStateFlow(false)
    val rolledToTomorrow: StateFlow<Boolean> = _rolledToTomorrow.asStateFlow()

    /** Non-null when scheduling failed (e.g. exact-alarm permission revoked). Reset after read. */
    private val _scheduleError = MutableStateFlow<String?>(null)
    val scheduleError: StateFlow<String?> = _scheduleError.asStateFlow()

    init {
        if (preselectedContactId != null) {
            viewModelScope.launch {
                _selectedContact.value = contactRepository.getContactById(preselectedContactId)
            }
        }
    }

    fun selectContact(contact: ContactEntity) { _selectedContact.value = contact }
    fun setMode(m: ScheduleMode) { _mode.value = m }
    fun setCountdownSeconds(s: Int) { _countdownSeconds.value = s.coerceAtLeast(1) }
    fun setCountdownMinutes(m: Int) { _countdownMinutes.value = m.coerceAtLeast(1) }
    fun setTargetHour(h: Int) { _targetHour.value = h }
    fun setTargetMinute(m: Int) { _targetMinute.value = m }

    fun schedule() {
        val contact = _selectedContact.value ?: return

        val triggerAt: Long = when (_mode.value) {
            ScheduleMode.COUNTDOWN_SECONDS -> {
                val secs = _countdownSeconds.value
                if (secs < 1) return
                System.currentTimeMillis() + secs * 1_000L
            }
            ScheduleMode.COUNTDOWN_MINUTES -> {
                val mins = _countdownMinutes.value
                if (mins < 1) return
                System.currentTimeMillis() + mins * 60_000L
            }
            ScheduleMode.TARGET_TIME -> resolveTargetTime()
        }

        viewModelScope.launch {
            try {
                callRepository.schedule(contact.id, triggerAt)
                _done.value = true
            } catch (e: SecurityException) {
                // Q10: exact-alarm permission was revoked — surface the error to the UI.
                _scheduleError.value = e.message ?: "Exact alarm permission not granted"
            }
        }
    }

    /** Resolves the user-chosen HH:MM to an absolute epoch millis.
     *  If the time has already passed today, rolls to tomorrow. */
    private fun resolveTargetTime(): Long {
        // Q9: Reset the flag first so repeat calls never show a stale snackbar.
        _rolledToTomorrow.value = false
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _targetHour.value)
            set(Calendar.MINUTE, _targetMinute.value)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            _rolledToTomorrow.value = true
        }
        return cal.timeInMillis
    }

    class Factory(
        private val contactRepo: ContactRepository,
        private val callRepo: CallRepository,
        private val preselectedContactId: Long?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ScheduleCallViewModel(contactRepo, callRepo, preselectedContactId) as T
        }
    }
}
