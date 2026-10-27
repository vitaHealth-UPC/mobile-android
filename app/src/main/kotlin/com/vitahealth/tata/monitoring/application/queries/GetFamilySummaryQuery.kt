package com.vitahealth.tata.monitoring.application.queries

import java.time.LocalDate
import java.time.ZoneId

/** The caller-selected calendar day and account-scoped summary projection. */
data class GetFamilySummaryQuery(
    val caregiverId: String,
    val olderAdultId: String,
    val olderAdultName: String,
    val day: LocalDate,
    val zone: ZoneId,
)
