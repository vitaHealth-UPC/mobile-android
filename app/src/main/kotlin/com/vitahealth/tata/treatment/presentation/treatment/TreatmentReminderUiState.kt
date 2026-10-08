package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.domain.model.ReminderPolicy

data class TreatmentReminderUiState(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val medicationId: String,
    val medicationLabel: String,
    val treatmentId: String,
    val treatmentName: String,
    val dosage: String,
    val frequency: String,
    val scheduleText: String,
    val instructions: String,
    val followUpDelayMinutes: String = "10",
    val validatedPolicy: ReminderPolicy? = null,
    val errorMessage: String? = null,
)
