package com.vitahealth.tata.analytics.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Verifies both analytics adapters against the production HTTP converter and status handling. */
class AnalyticsHttpBoundaryTest {
    @Test
    fun treatmentSummaryAlwaysSendsItsScopeAndNeverFallsBackOnFailure() = runBlocking {
        MockWebServer().use { server ->
            val repository =
                RemoteAdherenceSummaryRepository(
                    ApiClientFactory.create(server.url("/").toString()) { "token" }
                        .create(AnalyticsApiService::class.java)
                )
            for (status in listOf(204, 401, 403, 500)) {
                server.enqueue(MockResponse().setResponseCode(status))
                val result = repository.getTreatmentSummary("adult", "treatment / one", 7)
                if (status == 204) assertNull((result as AppResult.Success).value)
                else
                    assertEquals(
                        when (status) {
                            401 -> "SESSION_REQUIRED"
                            403 -> "ACCESS_DENIED"
                            else -> "REQUEST_FAILED"
                        },
                        (result as AppResult.Failure).code,
                    )
                val request = server.takeRequest()
                assertEquals(
                    "/api/v1/older-adults/adult/adherence/treatments/treatment%20%2F%20one/summary",
                    request.requestUrl?.encodedPath,
                )
                assertEquals("7", request.requestUrl?.queryParameter("days"))
                assertEquals("Bearer token", request.getHeader("Authorization"))
            }
            assertEquals(4, server.requestCount)
            assertEquals(
                "INVALID_REQUEST",
                (repository.getTreatmentSummary("adult", " ", 7) as AppResult.Failure).code,
            )
            assertEquals(4, server.requestCount)
        }
    }

    @Test
    fun authenticationAndAuthorizationAreNotGenericFailures() = runBlocking {
        for ((status, expected) in
            listOf(
                401 to "SESSION_REQUIRED",
                403 to "ACCESS_DENIED",
                400 to "INVALID_REQUEST",
                500 to "REQUEST_FAILED",
            )) {
            results(MockResponse().setResponseCode(status).setBody("{}")) { result ->
                assertEquals(expected, (result as AppResult.Failure).code)
            }
        }
    }

    @Test
    fun malformedJsonAndEmptySuccessAreInvalidResponses() = runBlocking {
        for (body in listOf("{\"broken\":", "null")) {
            results(MockResponse().setBody(body)) { result ->
                assertEquals("INVALID_RESPONSE", (result as AppResult.Failure).code)
            }
        }
    }

    @Test
    fun noContentPreservesInsufficientEvidence() = runBlocking {
        results(MockResponse().setResponseCode(204)) { result ->
            assertNull((result as AppResult.Success).value)
        }
    }

    private suspend fun results(response: MockResponse, verify: (AppResult<*>) -> Unit) {
        MockWebServer().use { server ->
            server.enqueue(response.clone())
            server.enqueue(response.clone())
            val api =
                ApiClientFactory.create(server.url("/").toString()) { "token" }
                    .create(AnalyticsApiService::class.java)
            verify(RemoteAdherenceSummaryRepository(api).getSummary("adult", 7))
            verify(RemoteAdherenceRecommendationsRepository(api).getRecommendations("adult", 7))
            assertEquals(2, server.requestCount)
        }
    }
}
