package com.vitahealth.tata.shared.infrastructure.network

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET

/** Exercises authentication changes against real HTTP requests from an existing API client. */
class ApiClientFactoryTest {
    @Test
    fun capturedSessionClientsReuseTransportWithoutSharingAuthorizationHeaders() {
        MockWebServer().use { server ->
            val first =
                ApiClientFactory.create(server.url("/").toString()) { "account-a" }
                    .create(ProbeApi::class.java)
            val second =
                ApiClientFactory.create(server.url("/").toString()) { "account-b" }
                    .create(ProbeApi::class.java)
            server.enqueue(MockResponse().setBody("{\"status\":\"ok\"}"))
            server.enqueue(MockResponse().setBody("{\"status\":\"ok\"}"))
            first.probe().execute()
            second.probe().execute()
            val a = server.takeRequest()
            val b = server.takeRequest()
            assertEquals("Bearer account-a", a.getHeader("Authorization"))
            assertEquals("Bearer account-b", b.getHeader("Authorization"))
            assertEquals(0, a.sequenceNumber)
            assertEquals(1, b.sequenceNumber)
        }
    }

    private interface ProbeApi {
        @GET("probe") fun probe(): Call<Map<String, String>>
    }

    @Test
    fun existingClientUsesCurrentCredentialsAndStopsSendingThemAfterLogout() {
        MockWebServer().use { server ->
            var token: String? = null
            val api =
                ApiClientFactory.create(server.url("/").toString()) { token }
                    .create(ProbeApi::class.java)

            for (currentToken in listOf(null, "first-session", "refreshed-session", null, " ")) {
                token = currentToken
                server.enqueue(MockResponse().setBody("{\"status\":\"ok\"}"))
                val response = api.probe().execute()
                assertEquals("ok", response.body()?.get("status"))
                val authorization = server.takeRequest().getHeader("Authorization")
                if (currentToken.isNullOrBlank()) {
                    assertNull(authorization)
                } else {
                    assertEquals("Bearer $currentToken", authorization)
                }
            }
        }
    }
}
