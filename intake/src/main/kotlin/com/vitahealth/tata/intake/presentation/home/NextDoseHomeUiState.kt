package com.vitahealth.tata.intake.presentation.home

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel

import com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress

sealed interface NextDoseHomeUiState {
    data object Loading : NextDoseHomeUiState

    data class NextDoseAvailable(
        val dose: NextDoseReadModel,
        val olderAdultName: String,
        val progress: DailyDoseProgress? = null,
    ) : NextDoseHomeUiState

    data class NoNextDose(
        val olderAdultName: String,
        val progress: DailyDoseProgress? = null,
    ) : NextDoseHomeUiState

    data class Error(
        val olderAdultName: String,
        val message: String,
    ) : NextDoseHomeUiState
}
