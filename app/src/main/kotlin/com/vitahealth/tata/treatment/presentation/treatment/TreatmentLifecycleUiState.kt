package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.domain.model.TreatmentStatus

data class TreatmentLifecycleUiState(
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
    val reminderDelayMinutes: Int,
    val status: TreatmentStatus = TreatmentStatus.DRAFT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
