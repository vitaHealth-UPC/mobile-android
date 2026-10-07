package com.vitahealth.tata.intake.presentation.home

import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel

data class DailyDoseProgress(val completed: Int, val total: Int)

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
