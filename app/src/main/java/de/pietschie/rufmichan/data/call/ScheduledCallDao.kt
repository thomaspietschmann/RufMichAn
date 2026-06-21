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

    /** Id of another call that is currently active (FIRED = ringing or in-call), or null. */
    @Query("SELECT id FROM scheduled_calls WHERE state = 'FIRED' AND id != :excludeId LIMIT 1")
    suspend fun getActiveFiredCallId(excludeId: Long): Long?

    /** Oldest call that was deferred because another call was active. */
    @Query("SELECT * FROM scheduled_calls WHERE state = 'WAITING' ORDER BY triggerAtEpochMillis ASC LIMIT 1")
    suspend fun getOldestWaitingCall(): ScheduledCallEntity?

    @Query("SELECT * FROM scheduled_calls WHERE state = 'WAITING'")
    suspend fun getWaitingCalls(): List<ScheduledCallEntity>

    @Insert
    suspend fun insert(call: ScheduledCallEntity): Long

    @Query("UPDATE scheduled_calls SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: CallState)

    @Query("UPDATE scheduled_calls SET triggerAtEpochMillis = :triggerAt, state = :state WHERE id = :id")
    suspend fun updateTriggerAndState(id: Long, triggerAt: Long, state: CallState)

    /** A FIRED row cannot survive a reboot, so any left at boot were interrupted calls. */
    @Query("UPDATE scheduled_calls SET state = 'MISSED' WHERE state = 'FIRED'")
    suspend fun markFiredCallsMissed()

    @Query("DELETE FROM scheduled_calls WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Delete
    suspend fun delete(call: ScheduledCallEntity)
}
