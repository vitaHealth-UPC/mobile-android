package com.vitahealth.tata.intake.presentation.home

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel

sealed interface NextDoseHomeUiState {
    data object Loading : NextDoseHomeUiState

    data class NextDoseAvailable(
        val dose: NextDoseReadModel,
        val olderAdultName: String,
    ) : NextDoseHomeUiState

    data class NoNextDose(
        val olderAdultName: String,
    ) : NextDoseHomeUiState

    data class Error(
        val olderAdultName: String,
        val message: String,
    ) : NextDoseHomeUiState
}
