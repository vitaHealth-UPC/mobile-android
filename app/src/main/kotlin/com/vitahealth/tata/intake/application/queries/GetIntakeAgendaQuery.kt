package com.vitahealth.tata.intake.application.queries

import java.time.Instant

/** Fetches an adult's scheduled doses in the specified half-open interval. */
data class GetIntakeAgendaQuery(
    val olderAdultId: String,
    val from: Instant,
    val to: Instant,
)
