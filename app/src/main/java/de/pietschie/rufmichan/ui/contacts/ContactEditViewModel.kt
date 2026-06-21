package de.pietschie.rufmichan.ui.contacts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.pietschie.rufmichan.data.contact.ContactEntity
import de.pietschie.rufmichan.data.contact.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactEditViewModel(
    private val contactRepository: ContactRepository,
    private val contactId: Long?
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _photoPath = MutableStateFlow<String?>(null)
    val photoPath: StateFlow<String?> = _photoPath.asStateFlow()

    private val _nameError = MutableStateFlow(false)
    val nameError: StateFlow<Boolean> = _nameError.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private var existingContact: ContactEntity? = null

    init {
        if (contactId != null) {
            viewModelScope.launch {
                contactRepository.getContactById(contactId)?.let { c ->
                    existingContact = c
                    _name.value = c.name
                    _phoneNumber.value = c.phoneNumber
                    _photoPath.value = c.photoPath
                }
            }
        }
    }

    fun onNameChange(v: String) {
        _name.value = v
        if (v.isNotBlank()) _nameError.value = false
    }

    fun onPhoneChange(v: String) { _phoneNumber.value = v }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val oldPath = _photoPath.value
            val newPath = contactRepository.savePhoto(uri)
            if (newPath != null) {
                _photoPath.value = newPath
                // Remove replaced photo file
                oldPath?.let { contactRepository.deletePhoto(it) }
            }
        }
    }

    fun save() {
        if (_name.value.isBlank()) {
            _nameError.value = true
            return
        }
        viewModelScope.launch {
            contactRepository.saveContact(
                ContactEntity(
                    id = existingContact?.id ?: 0L,
                    name = _name.value.trim(),
                    phoneNumber = _phoneNumber.value.trim(),
                    photoPath = _photoPath.value
                )
            )
            _saved.value = true
        }
    }

    class Factory(
        private val repo: ContactRepository,
        private val contactId: Long?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ContactEditViewModel(repo, contactId) as T
        }
    }
}
