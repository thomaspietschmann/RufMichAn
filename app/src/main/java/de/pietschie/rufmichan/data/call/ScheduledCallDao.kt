package de.pietschie.rufmichan.data.call

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledCallDao {

    @Transaction
    @Query("SELECT * FROM scheduled_calls WHERE state = 'SCHEDULED' ORDER BY triggerAtEpochMillis ASC")
    fun getActiveCallsWithContact(): Flow<List<CallWithContact>>

    @Transaction
    @Query("SELECT * FROM scheduled_calls WHERE state = 'SCHEDULED' AND triggerAtEpochMillis > :now")
    suspend fun getFutureScheduledCalls(now: Long): List<CallWithContact>

    @Transaction
    @Query("SELECT * FROM scheduled_calls WHERE id = :id")
    suspend fun getCallWithContactById(id: Long): CallWithContact?

    @Query("SELECT * FROM scheduled_calls WHERE id = :id")
    suspend fun getCallById(id: Long): ScheduledCallEntity?

    @Insert
    suspend fun insert(call: ScheduledCallEntity): Long

    @Query("UPDATE scheduled_calls SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: CallState)

    @Query("DELETE FROM scheduled_calls WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(call: ScheduledCallEntity)
}
