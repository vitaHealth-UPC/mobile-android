package com.vitahealth.tata.monitoring.domain.model

import java.time.Instant

/** Follow-up status of an alert, as the backend Alerts resource defines it. */
enum class AlertStatus { OPEN, ATTENDED, CLOSED }

/** Alert raised by an intake that was not confirmed in time, seen from the caregiver side. */
data class CaregiverAlert(
    val id: Long,
    val intakeId: String,
    val medicationName: String,
    val scheduledAt: Instant,
    val reason: String,
    val status: AlertStatus,
    val openedAt: Instant,
    val closedAt: Instant?,
) {
    init {
        require(id > 0) { "alert id must be positive" }
        require(medicationName.isNotBlank()) { "alert medication is required" }
        require(closedAt == null || !closedAt.isBefore(openedAt)) { "an alert cannot close before it opens" }
    }
}

/**
 * Follow-up moves a caregiver can request: attend an open alert, or close an open or attended one.
 * The backend keeps the final word and answers 409 when the alert already moved.
 */
fun AlertStatus.canMoveTo(target: AlertStatus): Boolean = when (this) {
    AlertStatus.OPEN -> target == AlertStatus.ATTENDED || target == AlertStatus.CLOSED
    AlertStatus.ATTENDED -> target == AlertStatus.CLOSED
    AlertStatus.CLOSED -> false
}
