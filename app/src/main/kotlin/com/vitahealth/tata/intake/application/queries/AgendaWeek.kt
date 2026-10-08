package com.vitahealth.tata.intake.application.queries

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class AgendaWeek(val start: LocalDate, val zone: ZoneId) {
    val days: List<LocalDate> get() = (0L..6L).map(start::plusDays)
    val from get() = start.atStartOfDay(zone).toInstant()
    val to get() = start.plusWeeks(1).atStartOfDay(zone).toInstant()
    companion object {
        fun containing(day: LocalDate, zone: ZoneId) = AgendaWeek(day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), zone)
    }
}
