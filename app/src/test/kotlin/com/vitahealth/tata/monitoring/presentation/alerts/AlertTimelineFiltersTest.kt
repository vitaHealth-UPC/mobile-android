package com.vitahealth.tata.monitoring.presentation.alerts

import com.vitahealth.tata.monitoring.alert
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFilter
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsUiState
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertTimelineFiltersTest {
    @Test
    fun filtersActualDoseOutcomesAndNeverCallsAnOverduePendingDoseUpcoming() {
        val now = Instant.parse("2026-10-24T15:00:00Z")
        fun dose(id: String, status: String, time: Instant = now) =
            MonitoredDose(id, "med", "Losartán", "1 comprimido", time, status)
        val state =
            AlertsUiState.Content(
                alerts = listOf(alert()),
                doses =
                    listOf(
                        dose("past", "PENDING", now.minusSeconds(1)),
                        dose("next", "PENDING", now.plusSeconds(1)),
                        dose("confirmed", "CONFIRMED"),
                        dose("late", "LATE"),
                        dose("omitted", "OMITTED"),
                    ),
                asOf = now,
            )
        assertEquals(listOf("next", "confirmed", "late"), state.visibleDoses.map { it.id })
        assertEquals(
            listOf("next"),
            state.copy(filter = AlertFilter.UPCOMING).visibleDoses.map { it.id },
        )
        assertEquals(emptyList<Any>(), state.copy(filter = AlertFilter.UPCOMING).visibleAlerts)
        assertEquals(
            listOf("confirmed", "late"),
            state.copy(filter = AlertFilter.COMPLETED).visibleDoses.map { it.id },
        )
        assertEquals(emptyList<Any>(), state.copy(filter = AlertFilter.PENDING).visibleDoses)
    }
}
