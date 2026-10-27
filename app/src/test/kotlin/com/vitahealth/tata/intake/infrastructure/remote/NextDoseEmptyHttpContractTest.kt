package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

/** Real Retrofit decoding distinguishes an authoritative 204 from failed/invalid reads. */
class NextDoseEmptyHttpContractTest {
    @Test
    fun onlyNoContentIsAnEmptyScheduleAndTheRequestCarriesItsSession() = runBlocking {
        MockWebServer().use { server ->
            val api =
                ApiClientFactory.create(server.url("/").toString()) { "test-session" }
                    .create(IntakeApiService::class.java)
            val repository = RemoteNextDoseRepository(api)
            server.enqueue(MockResponse().setResponseCode(204))
            val empty = repository.getNextDose("adult-1")
            assertTrue(empty is AppResult.Success)
            assertNull((empty as AppResult.Success).value)
            val request = server.takeRequest()
            assertEquals("/api/v1/older-adults/adult-1/intakes/next", request.path)
            assertEquals("Bearer test-session", request.getHeader("Authorization"))
            for (status in listOf(401, 403, 404, 500)) {
                server.enqueue(MockResponse().setResponseCode(status))
                assertTrue(
                    "HTTP $status must not become empty",
                    repository.getNextDose("adult-1") is AppResult.Failure,
                )
            }
            server.enqueue(MockResponse().setBody("null"))
            assertTrue(repository.getNextDose("adult-1") is AppResult.Failure)
        }
    }
}
