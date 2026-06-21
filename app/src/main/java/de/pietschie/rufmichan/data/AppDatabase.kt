package de.pietschie.rufmichan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.pietschie.rufmichan.data.call.CallWithContact
import de.pietschie.rufmichan.data.call.ScheduledCallDao
import de.pietschie.rufmichan.data.call.ScheduledCallEntity
import de.pietschie.rufmichan.data.contact.ContactDao
import de.pietschie.rufmichan.data.contact.ContactEntity

@Database(
    entities = [ContactEntity::class, ScheduledCallEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun scheduledCallDao(): ScheduledCallDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rufmichan.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
