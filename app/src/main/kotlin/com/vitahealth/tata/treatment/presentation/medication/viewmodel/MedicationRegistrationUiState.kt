package com.vitahealth.tata.treatment.presentation.medication.viewmodel

import com.vitahealth.tata.treatment.domain.model.entities.Medication

/** Registration fields, validation errors and saved medication for the first wizard step. */
data class MedicationRegistrationUiState(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val name: String = "",
    val presentation: String = "",
    val frequency: String = "",
    val timing: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val registeredMedication: Medication? = null,
    val errorMessage: String? = null,
)
