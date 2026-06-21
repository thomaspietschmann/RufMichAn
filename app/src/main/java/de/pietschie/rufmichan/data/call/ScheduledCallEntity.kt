package de.pietschie.rufmichan.data.call

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.pietschie.rufmichan.data.contact.ContactEntity

@Entity(
    tableName = "scheduled_calls",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId")]
)
data class ScheduledCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: Long,
    val triggerAtEpochMillis: Long,
    val state: CallState = CallState.SCHEDULED,
    val createdAtEpochMillis: Long
)
