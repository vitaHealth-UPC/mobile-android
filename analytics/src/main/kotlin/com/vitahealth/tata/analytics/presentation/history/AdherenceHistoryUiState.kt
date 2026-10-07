package com.vitahealth.tata.analytics.presentation.history

import com.vitahealth.tata.analytics.domain.model.AdherencePeriod

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

fun AdherencePeriod.label(): String = "Últimos $days días"

sealed interface AdherenceHistoryUiState {
    data object Loading : AdherenceHistoryUiState

    data class Content(
        val summary: AdherenceSummaryUi,
        val periodUpdated: Boolean = false,
    ) : AdherenceHistoryUiState

    data class InsufficientData(
        val periodLabel: String,
    ) : AdherenceHistoryUiState

    data class NoResults(
        val periodLabel: String,
    ) : AdherenceHistoryUiState

    data class Error(
        val message: String,
    ) : AdherenceHistoryUiState
}
