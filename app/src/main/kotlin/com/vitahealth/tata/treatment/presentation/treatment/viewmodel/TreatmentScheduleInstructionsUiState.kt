package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import com.vitahealth.tata.treatment.domain.model.valueobjects.ScheduleInstructions

/** Entered local times and instructions with their validated values and current error. */
data class TreatmentScheduleInstructionsUiState(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val medicationId: String,
    val medicationLabel: String,
    val treatmentId: String,
    val treatmentName: String,
    val dosage: String,
    val frequency: String,
    val scheduleText: String = "",
    val instructions: String = "",
    val validatedSchedule: ScheduleInstructions? = null,
    val errorMessage: String? = null,
)
