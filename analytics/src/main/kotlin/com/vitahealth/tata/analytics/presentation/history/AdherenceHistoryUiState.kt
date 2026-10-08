package com.vitahealth.tata.analytics.presentation.history

import com.vitahealth.tata.analytics.domain.model.AdherencePeriod
import com.vitahealth.tata.analytics.domain.model.IntakeOutcomeStatus

data class AdherenceTrendPoint(
    val label: String,
    val adherencePercent: Int,
)

data class RecentIntakeUi(
    val whenLabel: String,
    val medicationName: String,
    val outcomeText: String,
    val status: IntakeOutcomeStatus,
)

data class PatternUi(
    val headline: String,
    val summary: String,
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
    val recentIntakes: List<RecentIntakeUi> = emptyList(),
    val pattern: PatternUi? = null,
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
