package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.domain.model.Medication

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
