package com.vitahealth.tata.analytics.application.readmodels

data class RecommendationReadModel(
    val title: String,
    val description: String,
)

data class AdherenceRecommendationsReadModel(
    val periodDays: Int,
    val patternTitle: String,
    val patternSummary: String,
    val concentration: List<List<Float>>,
    val recommendations: List<RecommendationReadModel>,
)
