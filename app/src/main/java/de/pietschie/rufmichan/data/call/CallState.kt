package de.pietschie.rufmichan.data.call

enum class CallState {
    SCHEDULED,
    FIRED,
    // Fired while another call was still active; deferred until that call ends.
    WAITING,
    CANCELLED,
    COMPLETED,
    MISSED
}
