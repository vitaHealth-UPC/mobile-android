package com.vitahealth.tata.analytics.presentation.history

import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.application.readmodels.RecentIntakeReadModel
import com.vitahealth.tata.analytics.domain.model.valueobjects.IntakeOutcomeStatus
import com.vitahealth.tata.analytics.presentation.history.mappers.toUi
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdherenceHistoryUiMapperTest {
    @Test
    fun everyRowUsesTheSuppliedReferenceDate() {
        val today = LocalDate.of(2026, 10, 22)
        val facts =
            summary(
                listOf(
                    RecentIntakeReadModel(
                        today.atTime(8, 0),
                        "Losartán",
                        IntakeOutcomeStatus.OnTime,
                        null,
                    ),
                    RecentIntakeReadModel(
                        today.minusDays(1).atTime(20, 0),
                        "Amlodipino",
                        IntakeOutcomeStatus.Late,
                        24,
                    ),
                )
            )

        val display = facts.toUi(today)

        assertTrue(display.recentIntakes[0].whenLabel.startsWith("Hoy, "))
        assertTrue(display.recentIntakes[1].whenLabel.startsWith("Ayer, "))
        assertEquals("a tiempo", display.recentIntakes[0].outcomeText)
        assertEquals("24 min tarde", display.recentIntakes[1].outcomeText)
        assertTrue(facts.toUi(today.plusDays(1)).recentIntakes[0].whenLabel.startsWith("Ayer, "))
    }

    private fun summary(intakes: List<RecentIntakeReadModel>) =
        AdherenceSummaryReadModel(
            periodDays = 30,
            scheduledCount = 4,
            adherencePercent = 75,
            adherenceChangePercent = null,
            onTimePercent = 50,
            onTimeChangePercent = null,
            lateCount = 1,
            omittedCount = 1,
            trend = emptyList(),
            recentIntakes = intakes,
        )
}
