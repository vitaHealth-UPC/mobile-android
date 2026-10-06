package com.vitahealth.tata.intake.presentation.detail

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel

sealed interface DoseDetailUiState {
    data object Loading : DoseDetailUiState
    data class Content(val dose: DoseDetailReadModel) : DoseDetailUiState
    data class Error(val message: String) : DoseDetailUiState
}
