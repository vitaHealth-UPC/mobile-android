package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.domain.model.Treatment

data class TreatmentCreationUiState(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val medicationId: String,
    val medicationLabel: String,
    val name: String = "",
    val isLoading: Boolean = false,
    val createdTreatment: Treatment? = null,
    val errorMessage: String? = null,
)
