package com.vitahealth.tata.analytics.application.queries

/** Identifies the adult and period requested for adherence recommendations. */
data class GetAdherenceRecommendationsQuery(val olderAdultId: String, val periodDays: Int = 30)
