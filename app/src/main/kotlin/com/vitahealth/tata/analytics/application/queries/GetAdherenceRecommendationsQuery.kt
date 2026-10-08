package com.vitahealth.tata.analytics.application.queries

data class GetAdherenceRecommendationsQuery(
    val olderAdultId: String,
    val periodDays: Int = 30,
)
