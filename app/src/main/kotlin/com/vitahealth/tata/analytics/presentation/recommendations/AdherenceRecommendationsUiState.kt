package com.vitahealth.tata.analytics.presentation.recommendations

import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel

const val RECOMMENDATIONS_PERIOD_DAYS = 30

sealed interface AdherenceRecommendationsUiState {
    data object Loading : AdherenceRecommendationsUiState

    data class Content(
        val data: AdherenceRecommendationsReadModel,
    ) : AdherenceRecommendationsUiState

    data object InsufficientEvidence : AdherenceRecommendationsUiState

    data class Error(
        val message: String,
    ) : AdherenceRecommendationsUiState
}
