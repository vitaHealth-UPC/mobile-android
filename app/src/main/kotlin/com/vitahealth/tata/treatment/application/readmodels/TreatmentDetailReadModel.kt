package com.vitahealth.tata.treatment.application.readmodels

import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/**
 * Provides treatment identity, lifecycle state and optional regimen details for read-only views.
 */
data class TreatmentDetailReadModel(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val status: TreatmentStatus,
    val medicationId: String?,
    val dose: String?,
    val frequency: String?,
    val scheduledTimes: List<String>,
    val instructions: String,
    val reminderLeadMinutes: Int?,
    val followUpDelayMinutes: Int? = null,
)
