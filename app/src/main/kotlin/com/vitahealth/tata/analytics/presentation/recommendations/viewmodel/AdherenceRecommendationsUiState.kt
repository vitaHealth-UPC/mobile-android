package com.vitahealth.tata.analytics.presentation.recommendations.viewmodel

import com.vitahealth.tata.analytics.presentation.recommendations.ui.AdherenceRecommendationsUi

const val RECOMMENDATIONS_PERIOD_DAYS = 30

/** Loading, evidence and failure states of the recommendations screen. */
sealed interface AdherenceRecommendationsUiState {
    data object Loading : AdherenceRecommendationsUiState

    data class Content(val data: AdherenceRecommendationsUi) : AdherenceRecommendationsUiState

    data object InsufficientEvidence : AdherenceRecommendationsUiState

    data class Error(val message: String) : AdherenceRecommendationsUiState
}
