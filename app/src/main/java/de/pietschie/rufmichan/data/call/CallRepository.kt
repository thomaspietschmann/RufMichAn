package de.pietschie.rufmichan.data.call

import de.pietschie.rufmichan.alarm.CallScheduler
import kotlinx.coroutines.flow.Flow

class CallRepository(
    private val scheduledCallDao: ScheduledCallDao,
    private val callScheduler: CallScheduler
) {
    val activeCalls: Flow<List<CallWithContact>> = scheduledCallDao.getActiveCallsWithContact()

    /** Writes a new scheduled-call row and arms the alarm. Returns the new row id. */
    suspend fun schedule(contactId: Long, triggerAtEpochMillis: Long): Long {
        val entity = ScheduledCallEntity(
            contactId = contactId,
            triggerAtEpochMillis = triggerAtEpochMillis,
            state = CallState.SCHEDULED,
            createdAtEpochMillis = System.currentTimeMillis()
        )
        val id = scheduledCallDao.insert(entity)
        callScheduler.schedule(id, triggerAtEpochMillis)
        return id
    }

    /** Cancels the alarm and marks the row as CANCELLED. */
    suspend fun cancel(callId: Long) {
        callScheduler.cancel(callId)
        scheduledCallDao.updateState(callId, CallState.CANCELLED)
    }

    suspend fun markFired(callId: Long) = scheduledCallDao.updateState(callId, CallState.FIRED)
    suspend fun markCompleted(callId: Long) = scheduledCallDao.updateState(callId, CallState.COMPLETED)
    suspend fun markMissed(callId: Long) = scheduledCallDao.updateState(callId, CallState.MISSED)

    /** Returns all calls that are still SCHEDULED and have a future trigger time. */
    suspend fun getFutureScheduledCalls(): List<CallWithContact> =
        scheduledCallDao.getFutureScheduledCalls(System.currentTimeMillis())

    suspend fun getCallWithContact(id: Long): CallWithContact? =
        scheduledCallDao.getCallWithContactById(id)
}
