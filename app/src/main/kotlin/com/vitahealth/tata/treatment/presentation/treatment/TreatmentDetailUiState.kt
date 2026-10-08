package com.vitahealth.tata.treatment.presentation.treatment

import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

sealed interface TreatmentDetailUiState {
    data object Loading : TreatmentDetailUiState

    data class Loaded(
        val detail: TreatmentDetailReadModel,
        val olderAdultName: String,
        val medicationLabelHint: String,
    ) : TreatmentDetailUiState

    data class Restricted(
        val olderAdultName: String,
    ) : TreatmentDetailUiState

    data class NotFound(
        val olderAdultName: String,
    ) : TreatmentDetailUiState

    data class Error(
        val olderAdultName: String,
        val message: String,
    ) : TreatmentDetailUiState
}
