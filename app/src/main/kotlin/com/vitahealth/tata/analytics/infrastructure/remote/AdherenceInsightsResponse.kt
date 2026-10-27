package com.vitahealth.tata.analytics.infrastructure.remote

/** Detected pattern, concentration values and recommendation codes. */
data class AdherenceInsightsResponse(
    val periodDays: Int,
    val pattern: InsightPatternResponse,
    val concentration: List<List<Double>>,
    val recommendations: List<String>,
)
