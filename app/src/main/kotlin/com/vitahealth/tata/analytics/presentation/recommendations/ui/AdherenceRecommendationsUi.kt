package com.vitahealth.tata.analytics.presentation.recommendations.ui

/** Formatted pattern and recommendations displayed by the recommendations screen. */
data class AdherenceRecommendationsUi(
    val periodDays: Int,
    val patternTitle: String,
    val patternSummary: String,
    val concentration: List<List<Float>>,
    val recommendations: List<RecommendationUi>,
)
