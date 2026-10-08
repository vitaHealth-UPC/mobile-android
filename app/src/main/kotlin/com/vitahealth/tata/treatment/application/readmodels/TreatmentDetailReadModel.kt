package com.vitahealth.tata.treatment.application.readmodels

import com.vitahealth.tata.treatment.domain.model.TreatmentStatus

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
)
