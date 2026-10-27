package com.vitahealth.tata.omission.domain.model.valueobjects

import java.time.Instant

/** Notification evidence shared by versioned data messages and legacy topic delivery. */
data class OmissionPush(
    val kind: OmissionPushKind,
    val olderAdultId: String,
    val medicationName: String?,
    val deliveryId: String? = null,
    val intakeId: String? = null,
    val scheduledAt: Instant? = null,
) {
    init {
        require(olderAdultId.isNotBlank()) {
            "A push must identify the affected older adult"
        }
        require(deliveryId == null || deliveryId.isNotBlank()) {
            "Delivery ID cannot be blank when present"
        }
        require(intakeId == null || intakeId.isNotBlank()) {
            "Intake ID cannot be blank when present"
        }
    }
}
