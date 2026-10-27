package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.infrastructure.network.ApiClientFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class TreatmentReminderContractTest {
    private fun receipt(followUp: String) =
        """{
        "id":"treatment","olderAdultId":"adult","name":"Presion","status":"DRAFT",
        "medicationId":"medication","dose":"1 tableta","frequency":"DAILY",
        "scheduledTimes":["08:00:00"],"instructions":"Con agua",
        "reminderLeadMinutes":0,"followUpDelayMinutes":$followUp
    }"""

    @Test
    fun serializesAndConfirmsPostDoseDelaySeparatelyFromPreDoseLead() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody(receipt("25")))
            val repository =
                RemoteTreatmentLifecycleRepository(
                    ApiClientFactory.create(server.url("/").toString()) { "session" }
                        .create(TreatmentApiService::class.java)
                )
            val result =
                repository.configureTreatment(
                    "caregiver",
                    "treatment",
                    "medication",
                    "1 tableta",
                    "DAILY",
                    listOf("08:00"),
                    "Con agua",
                    0,
                    25,
                )
            assertTrue(result is AppResult.Success)
            val request = server.takeRequest()
            assertEquals("PUT", request.method)
            assertEquals("/api/v1/treatments/treatment/regimen", request.path)
            assertEquals("Bearer session", request.getHeader("Authorization"))
            val json = request.body.readUtf8()
            assertTrue(json.contains("\"reminderLeadMinutes\":0"))
            assertTrue(json.contains("\"followUpDelayMinutes\":25"))
        }
    }

    @Test
    fun rejectsOldOrDifferentServerReceiptRatherThanSilentlyActivatingWrongReminder() =
        runBlocking {
            MockWebServer().use { server ->
                val repository =
                    RemoteTreatmentLifecycleRepository(
                        ApiClientFactory.create(server.url("/").toString()) { null }
                            .create(TreatmentApiService::class.java)
                    )
                for (value in listOf("null", "10")) {
                    server.enqueue(MockResponse().setBody(receipt(value)))
                    assertTrue(
                        repository.configureTreatment(
                            "caregiver",
                            "treatment",
                            "medication",
                            "1 tableta",
                            "DAILY",
                            listOf("08:00"),
                            "Con agua",
                            0,
                            25,
                        ) is AppResult.Failure
                    )
                }
            }
        }
}
