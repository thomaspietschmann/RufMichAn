package de.pietschie.rufmichan.data.call

import de.pietschie.rufmichan.alarm.CallScheduler
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class CallRepository(
    private val scheduledCallDao: ScheduledCallDao,
    private val callScheduler: CallScheduler
) {
    val activeCalls: Flow<List<CallWithContact>> = scheduledCallDao.getActiveCallsWithContact()

    /** Writes a new scheduled-call row and arms the alarm. Returns the new row id.
     *  @throws SecurityException (forwarded from CallScheduler) if exact-alarm permission
     *  has been revoked — in that case the inserted row is rolled back automatically. */
    suspend fun schedule(contactId: Long, triggerAtEpochMillis: Long): Long {
        val entity = ScheduledCallEntity(
            contactId = contactId,
            triggerAtEpochMillis = triggerAtEpochMillis,
            state = CallState.SCHEDULED,
            createdAtEpochMillis = System.currentTimeMillis()
        )
        val id = scheduledCallDao.insert(entity)
        try {
            // Q10: arm the alarm AFTER inserting the row (we need the id for the PendingIntent).
            // On failure, roll the row back so no orphaned "scheduled" row is left behind.
            callScheduler.schedule(id, triggerAtEpochMillis)
        } catch (e: SecurityException) {
            scheduledCallDao.deleteById(id)
            throw e
        }
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

    /** Marks a call as deferred because another call was active when it fired. */
    suspend fun markWaiting(callId: Long) = scheduledCallDao.updateState(callId, CallState.WAITING)

    /** Id of another call currently active (ringing or in-call), or null if none. */
    suspend fun getActiveCallIdExcluding(callId: Long): Long? =
        scheduledCallDao.getActiveFiredCallId(callId)

    /** Marks [callId] COMPLETED and, if a call had to wait for it, re-arms the oldest
     *  waiting call to fire after a random 5–10 s gap (so it feels like a fresh call).
     *  Idempotent: if the call is already COMPLETED, does nothing (so the UI and the
     *  notification-decline path can't both promote the same waiting call twice). */
    suspend fun completeAndPromoteWaiting(callId: Long) {
        if (scheduledCallDao.getCallById(callId)?.state == CallState.COMPLETED) return
        scheduledCallDao.updateState(callId, CallState.COMPLETED)
        val next = scheduledCallDao.getOldestWaitingCall() ?: return
        rearmAt(next.id, System.currentTimeMillis() + Random.nextLong(5_000L, 10_001L))
    }

    /** Boot recovery: a call that was active (FIRED) at reboot was interrupted, so mark it
     *  MISSED, then re-arm any calls left WAITING behind it (staggered; the overlap guard
     *  serialises them one after another). */
    suspend fun recoverInterruptedCalls() {
        scheduledCallDao.markFiredCallsMissed()
        val now = System.currentTimeMillis()
        scheduledCallDao.getWaitingCalls().forEachIndexed { index, call ->
            rearmAt(call.id, now + 5_000L + index * 1_000L)
        }
    }

    private suspend fun rearmAt(callId: Long, triggerAtEpochMillis: Long) {
        scheduledCallDao.updateTriggerAndState(callId, triggerAtEpochMillis, CallState.SCHEDULED)
        try {
            callScheduler.schedule(callId, triggerAtEpochMillis)
        } catch (e: SecurityException) {
            // Exact-alarm permission revoked meanwhile; leave the row SCHEDULED to retry later.
        }
    }

    /** Returns all calls that are still SCHEDULED and have a future trigger time. */
    suspend fun getFutureScheduledCalls(): List<CallWithContact> =
        scheduledCallDao.getFutureScheduledCalls(System.currentTimeMillis())

    suspend fun getCallWithContact(id: Long): CallWithContact? =
        scheduledCallDao.getCallWithContactById(id)
}
