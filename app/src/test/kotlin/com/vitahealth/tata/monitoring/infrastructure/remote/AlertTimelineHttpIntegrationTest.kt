package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.queries.GetAlertTimelineQueryHandler
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

/** Exercises production Retrofit, JSON conversion, mapper and timeline query together. */
class AlertTimelineHttpIntegrationTest {
    private val day = LocalDate.of(2026, 10, 24)
    private val zone = ZoneId.of("America/Lima")

    private fun query(server: MockWebServer) =
        GetAlertTimelineQueryHandler(
            RemoteAlertTimelineReader(
                ApiClientFactory.create(server.url("/").toString()) { "test-session" }
                    .create(FamilyMonitoringApiService::class.java)
            )
        )

    @Test
    fun readsNonemptyAgendaWithRealJsonConversionAndCapturedAuthorization() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setBody(
                        """{"weeklyAdherence":{"confirmedIntakes":1,"totalIntakes":2},"openAlerts":[]}"""
                    )
            )
            server.enqueue(
                MockResponse()
                    .setBody(
                        """[
                {"id":"upcoming","medicationId":"med-1","medicationName":"Medication A","dose":"1 tablet","scheduledAt":"2026-10-24T19:00:00Z","status":"PENDING"},
                {"id":"confirmed","medicationId":"med-2","medicationName":"Medication B","dose":"1 tablet","scheduledAt":"2026-10-24T13:00:00Z","status":"CONFIRMED"}
            ]"""
                    )
            )
            server.enqueue(MockResponse().setBody("""{"id":"adult","fullName":"Rosa Vargas"}"""))
            val result = query(server)("caregiver", "adult", day, zone) as AppResult.Success
            assertEquals(listOf("confirmed", "upcoming"), result.value.doses.map { it.id })
            assertEquals(listOf("CONFIRMED", "PENDING"), result.value.doses.map { it.status })
            assertEquals("Rosa Vargas", result.value.olderAdultName)
            val status = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            val agenda = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            val profile = requireNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            assertEquals("/api/v1/older-adults/adult/status?caregiverId=caregiver", status.path)
            assertEquals("2026-10-24T05:00:00Z", agenda.requestUrl!!.queryParameter("from"))
            assertEquals("2026-10-25T05:00:00Z", agenda.requestUrl!!.queryParameter("to"))
            assertEquals("/api/v1/older-adults/adult", profile.path)
            listOf(status, agenda, profile).forEach {
                assertEquals("Bearer test-session", it.getHeader("Authorization"))
            }
        }
    }

    @Test
    fun forbiddenAgendaDiscardsStatusAndDoesNotRequestProfile() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setBody(
                        """{"weeklyAdherence":{"confirmedIntakes":0,"totalIntakes":0},"openAlerts":[]}"""
                    )
            )
            server.enqueue(
                MockResponse()
                    .setResponseCode(403)
                    .setBody("""{"code":"CARE_RELATIONSHIP_REQUIRED"}""")
            )
            val result = query(server)("caregiver", "adult", day, zone) as AppResult.Failure
            assertEquals(AlertFailureCodes.ACCESS_DENIED, result.code)
            assertEquals(2, server.requestCount)
        }
    }
}
