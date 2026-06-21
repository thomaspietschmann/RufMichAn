package de.pietschie.rufmichan.data.contact

import android.net.Uri
import de.pietschie.rufmichan.data.media.PhotoStorage
import kotlinx.coroutines.flow.Flow

class ContactRepository(
    private val contactDao: ContactDao,
    private val photoStorage: PhotoStorage
) {
    val contacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun getContactById(id: Long): ContactEntity? = contactDao.getContactById(id)

    /** Saves (insert or update) a contact and returns its id. */
    suspend fun saveContact(contact: ContactEntity): Long = contactDao.upsert(contact)

    /** Deletes the contact and its associated photo file. */
    suspend fun deleteContact(contact: ContactEntity) {
        contact.photoPath?.let { photoStorage.deletePhoto(it) }
        contactDao.delete(contact)
    }

    /** Copies the picked photo to internal storage and returns the absolute path. */
    fun savePhoto(uri: Uri): String? = photoStorage.savePhoto(uri)

    /** Removes an old photo file when replaced. */
    fun deletePhoto(path: String) = photoStorage.deletePhoto(path)
}
