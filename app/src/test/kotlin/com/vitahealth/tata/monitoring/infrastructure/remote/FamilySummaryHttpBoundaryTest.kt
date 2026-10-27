package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.JsonResponseBoundaryConverterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Exercises Retrofit conversion and real socket failures at the optional inventory boundary. */
class FamilySummaryHttpBoundaryTest {
    private fun summarize(inventory: MockResponse): AppResult<FamilySummary> = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse()
                    .setBody(
                        """{"weeklyAdherence":{"confirmedIntakes":1,"totalIntakes":1},"openAlerts":[]}"""
                    )
            )
            server.enqueue(
                MockResponse()
                    .setBody(
                        """[{"id":"intake","medicationId":"medication","medicationName":"Medicina","dose":"1 tableta","scheduledAt":"2026-10-06T13:00:00Z","status":"CONFIRMED"}]"""
                    )
            )
            server.enqueue(MockResponse().setResponseCode(204))
            server.enqueue(inventory)
            val client =
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .readTimeout(1, TimeUnit.SECONDS)
                    .build()
            val api =
                Retrofit.Builder()
                    .baseUrl(server.url("/"))
                    .client(client)
                    .addConverterFactory(JsonResponseBoundaryConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(FamilyMonitoringApiService::class.java)
            val result =
                RemoteFamilyMonitoringRepository(api)
                    .summary(
                        "caregiver",
                        "adult",
                        "Ana",
                        LocalDate.of(2026, 10, 6),
                        ZoneId.of("America/Bogota"),
                    )
            assertEquals(4, server.requestCount)
            result
        }
    }

    @Test
    fun missingStockScalarsAreInvalidWhileExplicitZeroAndFalseRemainValid() {
        for (json in
            listOf(
                """{"medicationId":"medication","lowStock":false}""",
                """{"medicationId":"medication","remainingStock":0}""",
                """{"medicationId":"medication","remainingStock":null,"lowStock":false}""",
            )) {
            assertEquals(
                "INVALID_RESPONSE",
                (summarize(MockResponse().setBody(json)) as AppResult.Failure).code,
            )
        }
        val valid =
            summarize(
                MockResponse()
                    .setBody(
                        """{"medicationId":"medication","remainingStock":0,"lowStock":false}"""
                    )
            )
                as AppResult.Success
        assertTrue(valid.value.inventoryAvailable)
        assertTrue(valid.value.stockAttention.isEmpty())
    }

    @Test
    fun disconnectedInventoryPreservesConfirmedIntakeEvidence() {
        val result =
            summarize(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))
                as AppResult.Success
        assertFalse(result.value.inventoryAvailable)
        assertEquals(1, result.value.today.confirmed)
    }

    @Test
    fun malformedInventoryJsonIsNotAConnectivityFallback() {
        val result = summarize(MockResponse().setBody("{\"medicationId\":")) as AppResult.Failure
        assertEquals("INVALID_RESPONSE", result.code)
    }

    @Test
    fun revokedInventoryAccessRejectsTheProtectedSummary() {
        for ((status, code) in
            listOf(401 to "SESSION_REQUIRED", 403 to "CARE_RELATIONSHIP_REQUIRED")) {
            assertEquals(
                code,
                (summarize(MockResponse().setResponseCode(status)) as AppResult.Failure).code,
            )
        }
    }

    @Test
    fun missingInventoryIsAnAllowedAbsence() {
        val result = summarize(MockResponse().setResponseCode(404)) as AppResult.Success
        assertTrue(result.value.inventoryAvailable)
        assertTrue(result.value.stockAttention.isEmpty())
    }

    @Test
    fun successfulInventoryWithoutBodyIsInvalid() {
        assertEquals(
            "INVALID_RESPONSE",
            (summarize(MockResponse().setResponseCode(204)) as AppResult.Failure).code,
        )
    }
}
