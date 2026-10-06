package com.vitahealth.tata.analytics.presentation.history

data class AdherenceTrendPoint(
    val label: String,
    val adherencePercent: Int,
)

data class AdherenceSummaryUi(
    val periodLabel: String,
    val adherencePercent: Int,
    val adherenceChangeText: String?,
    val onTimePercent: Int,
    val onTimeChangeText: String?,
    val lateCount: Int,
    val omittedCount: Int,
    val lateOmittedCaption: String,
    val trend: List<AdherenceTrendPoint>,
)

sealed interface AdherenceHistoryUiState {
    data object Loading : AdherenceHistoryUiState

    data class Content(
        val summary: AdherenceSummaryUi,
    ) : AdherenceHistoryUiState

    data class InsufficientData(
        val periodLabel: String,
    ) : AdherenceHistoryUiState

    data class Error(
        val message: String,
    ) : AdherenceHistoryUiState
}
