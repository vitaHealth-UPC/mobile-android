package com.vitahealth.tata.monitoring.presentation.summary

import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryOutcome
import com.vitahealth.tata.monitoring.presentation.summary.mappers.toHistoryRow
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class IntakeHistoryUiMapperTest {
    @Test
    fun preservesSpanishOutcomeAndUsesRequestedTimeZone() {
        val entry =
            IntakeHistoryEntry(
                "Medicina",
                Instant.parse("2026-10-06T13:00:00Z"),
                IntakeHistoryOutcome.LATE,
            )
        val row = entry.toHistoryRow(ZoneId.of("America/Bogota"), Locale.US)
        assertEquals("Medicina · Confirmada con retraso\n6 Oct, 8:00 AM", row)
    }
}
