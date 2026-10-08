package com.vitahealth.tata.app

import com.google.gson.Gson
import com.vitahealth.tata.monitoring.domain.model.AlertStatus
import com.vitahealth.tata.monitoring.infrastructure.remote.AlertSummaryResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.StatusResponse
import com.vitahealth.tata.monitoring.infrastructure.remote.UpdateAlertStatusRequest
import com.vitahealth.tata.monitoring.infrastructure.remote.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Parses the payloads published in the backend Swagger with the same Gson converter the app's Retrofit uses.
 * It lives in `:app` because `:monitoring` does not depend on Gson.
 */
class AlertsContractTest {
    private val gson = Gson()

    private val alertJson = """
        {
          "id": 1,
          "intakeId": "101",
          "medicationName": "Losartan 50 mg",
          "scheduledAt": "2026-10-05T13:00:00Z",
          "reason": "Intake not confirmed within the grace period",
          "status": "OPEN",
          "openedAt": "2026-10-05T13:30:00Z",
          "closedAt": null
        }
    """.trimIndent()

    @Test fun alertDetailMapsToTheDomain() {
        val alert = gson.fromJson(alertJson, AlertSummaryResponse::class.java).toDomain()

        assertEquals(1L, alert.id)
        assertEquals("101", alert.intakeId)
        assertEquals(AlertStatus.OPEN, alert.status)
        assertEquals(Instant.parse("2026-10-05T13:00:00Z"), alert.scheduledAt)
        assertNull(alert.closedAt)
    }

    @Test fun int64IdentifiersSurviveTheConverter() {
        val json = alertJson.replace("\"id\": 1,", "\"id\": 9007199254740993,")

        assertEquals(9_007_199_254_740_993L, gson.fromJson(json, AlertSummaryResponse::class.java).toDomain().id)
    }

    @Test fun closedAlertKeepsItsClosingTime() {
        val json = alertJson.replace("\"OPEN\"", "\"CLOSED\"").replace("\"closedAt\": null", "\"closedAt\": \"2026-10-05T15:00:00Z\"")

        val alert = gson.fromJson(json, AlertSummaryResponse::class.java).toDomain()

        assertEquals(AlertStatus.CLOSED, alert.status)
        assertEquals(Instant.parse("2026-10-05T15:00:00Z"), alert.closedAt)
    }

    @Test fun statusCarriesTheOpenAlerts() {
        val json = """
            {
              "nextIntakeAt": "2026-10-05T21:00:00Z",
              "lastIntakeStatus": "CONFIRMED",
              "hasOpenAlert": true,
              "openAlerts": [$alertJson],
              "weeklyAdherence": { "confirmedIntakes": 3, "totalIntakes": 4 },
              "lowStock": [],
              "adherenceInsights": []
            }
        """.trimIndent()

        val status = gson.fromJson(json, StatusResponse::class.java)

        assertEquals(listOf(1L), status.openAlerts.orEmpty().map { it.toDomain().id })
        assertEquals(4, status.weeklyAdherence.totalIntakes)
    }

    @Test fun statusWithoutAlertsHasAnEmptyList() {
        val json = """{ "hasOpenAlert": false, "openAlerts": [], "weeklyAdherence": { "confirmedIntakes": 0, "totalIntakes": 0 } }"""

        assertTrue(gson.fromJson(json, StatusResponse::class.java).openAlerts.orEmpty().isEmpty())
    }

    @Test fun statusUpdateRequestMatchesUpdateAlertStatusResource() {
        assertEquals("""{"status":"ATTENDED"}""", gson.toJson(UpdateAlertStatusRequest(AlertStatus.ATTENDED.name)))
    }

    @Test fun statusUpdateAnswerIsTheAttendedAlert() {
        val json = alertJson.replace("\"OPEN\"", "\"ATTENDED\"")

        assertEquals(AlertStatus.ATTENDED, gson.fromJson(json, AlertSummaryResponse::class.java).toDomain().status)
    }
}

