package de.pietschie.rufmichan.data.call

import androidx.room.Embedded
import androidx.room.Relation
import de.pietschie.rufmichan.data.contact.ContactEntity

data class CallWithContact(
    @Embedded val call: ScheduledCallEntity,
    @Relation(
        parentColumn = "contactId",
        entityColumn = "id"
    )
    val contact: ContactEntity
)
