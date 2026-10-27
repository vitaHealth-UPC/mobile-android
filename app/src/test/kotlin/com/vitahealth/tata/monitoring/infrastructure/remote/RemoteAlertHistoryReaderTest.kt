package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class RemoteAlertHistoryReaderTest {
    @Test
    fun readsActualDeliveryEvidenceFromProductionRetrofit() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setBody(
                        """[
                {"type":"ALERT_GENERATED","occurredAt":"2026-10-25T13:30:00Z"},
                {"type":"INTAKE_SCHEDULED","occurredAt":"2026-10-25T13:00:00Z"},
                {"type":"REINFORCED_REMINDER_SENT","occurredAt":"2026-10-25T13:10:00Z"}
            ]"""
                    )
            )
            val reader =
                RemoteAlertHistoryReader(
                    ApiClientFactory.create(server.url("/").toString()) { "captured" }
                        .create(FamilyMonitoringApiService::class.java)
                )
            val result = reader.read("caregiver", "adult", 1) as AppResult.Success
            assertEquals(RecordedAlertEvent.Type.REINFORCED_REMINDER_SENT, result.value[1].type)
            val request = server.takeRequest()
            assertEquals(
                "/api/v1/older-adults/adult/alerts/1/timeline?caregiverId=caregiver",
                request.path,
            )
            assertEquals("Bearer captured", request.getHeader("Authorization"))
        }
    }

    @Test
    fun readsPersistedOpenAndClosedTimelinesWithoutInventingReminderDelivery() = runBlocking {
        MockWebServer().use { server ->
            val reader =
                RemoteAlertHistoryReader(
                    ApiClientFactory.create(server.url("/").toString()) { "captured" }
                        .create(FamilyMonitoringApiService::class.java)
                )
            for (closed in listOf(false, true)) {
                val closure =
                    if (closed) """,{"type":"ALERT_CLOSED","occurredAt":"2026-10-25T14:00:00Z"}"""
                    else ""
                server.enqueue(
                    MockResponse()
                        .setBody(
                            """[{"type":"INTAKE_SCHEDULED","occurredAt":"2026-10-25T13:00:00Z"},
                    {"type":"ALERT_GENERATED","occurredAt":"2026-10-25T13:30:00Z"}$closure]"""
                        )
                )
                val result = reader.read("caregiver", "adult", 1) as AppResult.Success
                assertEquals(if (closed) 3 else 2, result.value.size)
                assertFalse(
                    result.value.any { it.type == RecordedAlertEvent.Type.REINFORCED_REMINDER_SENT }
                )
                assertEquals(
                    closed,
                    result.value.any { it.type == RecordedAlertEvent.Type.ALERT_CLOSED },
                )
            }
        }
    }

    @Test
    fun rejectsUnknownOrMissingEventsAndPropagatesAccessDenial() = runBlocking {
        MockWebServer().use { server ->
            val reader =
                RemoteAlertHistoryReader(
                    ApiClientFactory.create(server.url("/").toString()) { null }
                        .create(FamilyMonitoringApiService::class.java)
                )
            listOf("[]", """[{"type":"INVENTED","occurredAt":"2026-10-25T13:00:00Z"}]""").forEach {
                json ->
                server.enqueue(MockResponse().setBody(json))
                assertEquals(
                    AlertFailureCodes.INVALID_RESPONSE,
                    (reader.read("caregiver", "adult", 1) as AppResult.Failure).code,
                )
            }
            server.enqueue(MockResponse().setResponseCode(403))
            assertEquals(
                AlertFailureCodes.ACCESS_DENIED,
                (reader.read("caregiver", "adult", 1) as AppResult.Failure).code,
            )
        }
    }
}
