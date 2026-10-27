package com.vitahealth.tata.analytics.application.readmodels

import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import java.time.LocalDateTime

/** Medication outcome at its scheduled local date and time. */
data class RecentIntakeReadModel(
    val scheduledAt: LocalDateTime,
    val medicationName: String,
    val status: IntakeOutcomeStatus,
    val minutesLate: Int?,
)
