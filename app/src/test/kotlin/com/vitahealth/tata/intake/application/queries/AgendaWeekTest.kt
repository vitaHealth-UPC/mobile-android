package com.vitahealth.tata.intake.application.queries

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class AgendaWeekTest {
    @Test fun weekUsesLocalMondayAndExclusiveNextMonday() {
        val week = AgendaWeek.containing(LocalDate.of(2026, 10, 6), ZoneId.of("America/Bogota"))
        assertEquals(LocalDate.of(2026, 10, 5), week.start)
        assertEquals(Instant.parse("2026-10-05T05:00:00Z"), week.from)
        assertEquals(Instant.parse("2026-10-12T05:00:00Z"), week.to)
        assertEquals(7, week.days.size)
    }
    @Test fun calendarWeekAcrossDstIsNotFixed168Hours() {
        val week = AgendaWeek.containing(LocalDate.of(2026, 11, 1), ZoneId.of("America/New_York"))
        assertEquals(169, Duration.between(week.from, week.to).toHours())
        assertEquals(LocalDate.of(2026, 11, 2), week.to.atZone(week.zone).toLocalDate())
    }
}
