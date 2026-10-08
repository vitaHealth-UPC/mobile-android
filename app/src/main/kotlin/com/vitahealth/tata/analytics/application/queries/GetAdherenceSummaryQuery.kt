package com.vitahealth.tata.analytics.application.queries

data class GetAdherenceSummaryQuery(
    val olderAdultId: String,
    val periodDays: Int = 30,
)
