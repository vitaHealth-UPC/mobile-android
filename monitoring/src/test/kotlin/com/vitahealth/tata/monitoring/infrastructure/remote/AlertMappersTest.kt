package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.swaggerAlertResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class AlertMappersTest {

    @Test fun mapsTheSwaggerExampleWithoutClosingTime() {
        val alert = swaggerAlertResponse().toDomain()

        assertEquals(1L, alert.id)
        assertEquals("101", alert.intakeId)
        assertEquals("Losartan 50 mg", alert.medicationName)
        assertEquals(Instant.parse("2026-10-05T13:00:00Z"), alert.scheduledAt)
        assertEquals(Instant.parse("2026-10-05T13:30:00Z"), alert.openedAt)
        assertEquals(AlertStatus.OPEN, alert.status)
        assertNull(alert.closedAt)
    }

    @Test fun keepsInt64IdentifiersAboveTheIntRange() {
        assertEquals(9_007_199_254_740_993L, swaggerAlertResponse(id = 9_007_199_254_740_993L).toDomain().id)
    }

    @Test fun mapsEveryBackendStatus() {
        assertEquals(AlertStatus.OPEN, alertStatusOf("OPEN"))
        assertEquals(AlertStatus.ATTENDED, alertStatusOf("ATTENDED"))
        assertEquals(AlertStatus.CLOSED, alertStatusOf("CLOSED"))
    }

    @Test fun readsTheClosingTimeOfAClosedAlert() {
        val alert = swaggerAlertResponse(status = "CLOSED", closedAt = "2026-10-05T15:00:00Z").toDomain()

        assertEquals(AlertStatus.CLOSED, alert.status)
        assertEquals(Instant.parse("2026-10-05T15:00:00Z"), alert.closedAt)
    }

    @Test fun acceptsDateTimesWithAnOffset() {
        val alert = swaggerAlertResponse().copy(scheduledAt = "2026-10-05T08:00:00-05:00").toDomain()

        assertEquals(Instant.parse("2026-10-05T13:00:00Z"), alert.scheduledAt)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTheResolvedLabelBecauseTheBackendHasNoSuchStatus() {
        alertStatusOf("RESOLVED")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnAlertWithoutId() {
        swaggerAlertResponse(id = null).toDomain()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsAnInvalidDate() {
        swaggerAlertResponse().copy(openedAt = "yesterday").toDomain()
    }
}
