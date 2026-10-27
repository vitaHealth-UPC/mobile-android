package com.vitahealth.tata.analytics.application.queries

/** Identifies the adult and period requested for adherence history. */
data class GetAdherenceSummaryQuery(val olderAdultId: String, val periodDays: Int = 30)
