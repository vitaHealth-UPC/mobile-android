package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercises the production Retrofit converter and exact reactivation receipt contract. */
class MedicationReactivationHttpTest {
    @Test
    fun onlyAnActiveReceiptForTheRequestedMedicationIsAccepted() = runBlocking {
        MockWebServer().use { server ->
            val repository =
                RemoteMedicationManagementRepository(
                    ApiClientFactory.create(server.url("/").toString()) { "token" }
                        .create(TreatmentApiService::class.java)
                )
            for ((id, active, expectedSuccess) in
                listOf(
                    Triple("medication", true, true),
                    Triple("other", true, false),
                    Triple("medication", false, false),
                )) {
                server.enqueue(
                    MockResponse()
                        .setBody(
                            """{"id":"$id","olderAdultId":"adult","name":"Losartan","presentation":"50 mg","active":$active}"""
                        )
                )
                val result = repository.reactivate("caregiver", "medication")
                assertEquals(expectedSuccess, result is AppResult.Success)
                val request = server.takeRequest()
                assertEquals("POST", request.method)
                assertEquals(
                    "/api/v1/medications/medication/reactivation",
                    request.requestUrl?.encodedPath,
                )
                assertEquals("caregiver", request.requestUrl?.queryParameter("caregiverId"))
                assertEquals("Bearer token", request.getHeader("Authorization"))
            }
            assertEquals(3, server.requestCount)
        }
    }

    @Test
    fun authorizationAndMissingEndpointsNeverBecomeSuccessfulReactivation() = runBlocking {
        MockWebServer().use { server ->
            val repository =
                RemoteMedicationManagementRepository(
                    ApiClientFactory.create(server.url("/").toString()) { "token" }
                        .create(TreatmentApiService::class.java)
                )
            for ((status, code) in
                listOf(
                    401 to "SESSION_REQUIRED",
                    403 to "CARE_LINK_NOT_AUTHORIZED",
                    404 to "RESOURCE_NOT_FOUND",
                    409 to "CONFLICT",
                    500 to "REQUEST_FAILED",
                )) {
                server.enqueue(MockResponse().setResponseCode(status))
                assertEquals(
                    code,
                    (repository.reactivate("caregiver", "medication") as AppResult.Failure).code,
                )
                server.takeRequest()
            }
            assertEquals(5, server.requestCount)
        }
    }
}
