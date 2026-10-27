package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import com.vitahealth.tata.treatment.domain.model.valueobjects.RegimenBasics

/** Entered dose and frequency with their validated domain values or validation error. */
data class TreatmentDoseFrequencyUiState(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val medicationId: String,
    val medicationLabel: String,
    val treatmentId: String,
    val treatmentName: String,
    val dosage: String = "",
    val frequency: String = "",
    val validatedBasics: RegimenBasics? = null,
    val errorMessage: String? = null,
)
