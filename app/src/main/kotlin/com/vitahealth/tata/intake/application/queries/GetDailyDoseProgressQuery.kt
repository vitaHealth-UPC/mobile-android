package com.vitahealth.tata.intake.application.queries

import java.time.LocalDate
import java.time.ZoneId

data class GetDailyDoseProgressQuery(
    val olderAdultId: String,
    val day: LocalDate,
    val zone: ZoneId,
)
