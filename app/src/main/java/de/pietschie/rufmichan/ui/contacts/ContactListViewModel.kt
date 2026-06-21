package de.pietschie.rufmichan.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.pietschie.rufmichan.data.contact.ContactEntity
import de.pietschie.rufmichan.data.contact.ContactRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactListViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    val contacts: StateFlow<List<ContactEntity>> = contactRepository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(contact: ContactEntity) {
        viewModelScope.launch { contactRepository.deleteContact(contact) }
    }

    class Factory(private val repo: ContactRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ContactListViewModel(repo) as T
        }
    }
}
