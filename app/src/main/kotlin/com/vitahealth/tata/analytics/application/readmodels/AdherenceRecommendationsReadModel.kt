package com.vitahealth.tata.analytics.application.readmodels

import com.vitahealth.tata.analytics.domain.model.valueobjects.AdherenceRecommendation

/** Typed insight facts, concentration values and supported recommendation actions. */
data class AdherenceRecommendationsReadModel(
    val periodDays: Int,
    val pattern: AdherenceInsightPatternReadModel,
    val concentration: List<List<Float>>,
    val recommendations: List<AdherenceRecommendation>,
)
