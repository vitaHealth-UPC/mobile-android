package com.vitahealth.tata.analytics.presentation.history.viewmodel

import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceSummaryUi

/** Spanish label shared by the history content and empty states. */
fun AdherencePeriod.label(): String = "Últimos $days días"

/** Loading, content, empty and error outcomes of the selected history period. */
sealed interface AdherenceHistoryUiState {
    data object Loading : AdherenceHistoryUiState

    data class Content(val summary: AdherenceSummaryUi, val periodUpdated: Boolean = false) :
        AdherenceHistoryUiState

    data class InsufficientData(val periodLabel: String) : AdherenceHistoryUiState

    data class NoResults(val periodLabel: String) : AdherenceHistoryUiState

    data class Error(val message: String) : AdherenceHistoryUiState
}
