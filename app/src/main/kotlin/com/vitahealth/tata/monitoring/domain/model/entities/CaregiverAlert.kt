package com.vitahealth.tata.monitoring.domain.model.entities

import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import java.time.Instant

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
        require(intakeId.isNotBlank()) { "alert intake is required" }
        require((status == AlertStatus.CLOSED) == (closedAt != null)) {
            "closed alerts must carry their closing instant"
        }
        require(medicationName.isNotBlank()) { "alert medication is required" }
        require(closedAt == null || !closedAt.isBefore(openedAt)) {
            "an alert cannot close before it opens"
        }
    }
}
