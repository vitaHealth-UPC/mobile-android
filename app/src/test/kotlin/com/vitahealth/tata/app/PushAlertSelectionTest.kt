package com.vitahealth.tata.app

import com.vitahealth.tata.app.navigation.alertForPush
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.CaregiverAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/** A tapped caregiver push opens the alert of its medication (US-22 deep link to US-27). */
class PushAlertSelectionTest {
    private fun alert(id: Long, medication: String, status: AlertStatus, openedAt: String) = CaregiverAlert(
        id = id, intakeId = "i$id", medicationName = medication,
        scheduledAt = Instant.parse(openedAt).minusSeconds(1800), reason = "Intake not confirmed within the grace period",
        status = status, openedAt = Instant.parse(openedAt),
        closedAt = if (status == AlertStatus.CLOSED) Instant.parse(openedAt).plusSeconds(60) else null,
    )

    private val alerts = listOf(
        alert(1, "Losartan 50 mg", AlertStatus.OPEN, "2026-10-05T13:30:00Z"),
        alert(2, "Metformin 850 mg", AlertStatus.OPEN, "2026-10-05T20:00:00Z"),
        alert(3, "Losartan 50 mg", AlertStatus.ATTENDED, "2026-10-06T13:30:00Z"),
    )

    @Test fun picksTheOpenAlertOfTheMedication() {
        assertEquals(1L, alertForPush(alerts, "losartan 50 mg")?.id)
    }

    @Test fun withoutAMatchPicksTheMostRecentOpenAlert() {
        assertEquals(2L, alertForPush(alerts, null)?.id)
        assertEquals(2L, alertForPush(alerts, "Aspirin")?.id)
    }

    @Test fun withoutOpenAlertsOpensOnlyTheList() {
        assertNull(alertForPush(alerts.filter { it.status != AlertStatus.OPEN }, "Losartan 50 mg"))
    }
}
