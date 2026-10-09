package com.vitahealth.tata.intake.application.readmodels

import com.vitahealth.tata.intake.domain.model.DoseStatus
import java.time.Instant

data class DoseDetailReadModel(
    val id: String,
    val treatmentId: String,
    val medicationId: String,
    val olderAdultId: String,
    val medicationName: String,
    val dose: String,
    val instructions: String,
    val scheduledAt: Instant,
    val status: DoseStatus,
    val confirmedAt: Instant? = null,
    val alreadyConfirmed: Boolean = false,
)
