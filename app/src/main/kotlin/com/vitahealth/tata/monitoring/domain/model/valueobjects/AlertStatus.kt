package com.vitahealth.tata.monitoring.domain.model.valueobjects

/** Follow-up status of an alert, as the backend Alerts resource defines it. */
enum class AlertStatus {
    OPEN,
    ATTENDED,
    CLOSED,
}

/**
 * Follow-up moves a caregiver can request: attend an open alert, or close an open or attended one.
 * The backend keeps the final word and answers 409 when the alert already moved.
 */
fun AlertStatus.canMoveTo(target: AlertStatus): Boolean =
    when (this) {
        AlertStatus.OPEN -> target == AlertStatus.ATTENDED || target == AlertStatus.CLOSED
        AlertStatus.ATTENDED -> target == AlertStatus.CLOSED
        AlertStatus.CLOSED -> false
    }
