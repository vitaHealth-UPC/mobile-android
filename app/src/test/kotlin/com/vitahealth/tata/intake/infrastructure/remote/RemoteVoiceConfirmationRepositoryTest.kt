package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteVoiceConfirmationRepositoryTest {
    private val command = ConfirmDoseByVoiceCommand("intake-1", "recording".toByteArray(), "audio/mp4", "es-419")
    private val intake = """{"id":"intake-1","treatmentId":"t-1","medicationId":"m-1","olderAdultId":"adult-1","medicationName":"Losartán","dose":"1 comprimido","scheduledAt":"2026-10-08T13:00:00Z","status":"CONFIRMED","confirmedAt":"2026-10-08T13:01:00Z","confirmationChannel":"VOICE"}"""

    private fun request(json: String, status: Int = 200, verify: (AppResult<com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationReadModel>, okhttp3.mockwebserver.RecordedRequest) -> Unit) = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(json))
            server.start()
            val api = Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build().create(VoiceConfirmationApiService::class.java)
            val result = RemoteVoiceConfirmationRepository(api).confirm(command)
            verify(result, server.takeRequest())
        }
    }

    @Test fun sendsAudioToTheVoiceEndpointAndUsesServerConfirmation() {
        request("""{"status":"CONFIRMED","intake":$intake}""") { result, sent ->
            assertEquals("/api/v1/intakes/intake-1/voice-confirmation?language=es-419", sent.path)
            assertTrue(sent.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
            val body = sent.body.readUtf8()
            assertTrue(body.contains("name=\"audio\""))
            assertTrue(body.contains("audio/mp4"))
            assertTrue(body.contains("recording"))
            assertEquals(VoiceConfirmationStatus.CONFIRMED, (result as AppResult.Success).value.status)
            assertNotNull(result.value.intake!!.confirmedAt)
        }
    }

    @Test fun repeatedVoiceConfirmationPreservesOriginalDate() {
        request("""{"status":"ALREADY_CONFIRMED","intake":$intake}""") { result, _ ->
            val dose = (result as AppResult.Success).value.intake!!
            assertTrue(dose.alreadyConfirmed)
            assertEquals("2026-10-08T13:01:00Z", dose.confirmedAt.toString())
        }
    }

    @Test fun unrecognizedAndUnavailableResultsNeverFabricateAnIntake() {
        for (status in listOf("NOT_RECOGNIZED", "NOT_VALIDATED", "PROVIDER_UNAVAILABLE")) {
            request("""{"status":"$status"}""") { result, _ ->
                val value = (result as AppResult.Success).value
                assertEquals(status, value.status.name)
                assertNull(value.intake)
            }
        }
    }

    @Test fun missingAndForeignIntakesCannotBePresentedAsConfirmed() {
        for (body in listOf("""{"status":"CONFIRMED"}""", """{"status":"CONFIRMED","intake":${intake.replace("intake-1", "other")}}""", """{"status":"CONFIRMED","intake":${intake.replace("CONFIRMED", "PENDING")}}""")) {
            request(body) { result, _ -> assertEquals("INVALID_VOICE_RESULT", (result as AppResult.Failure).code) }
        }
    }

    @Test fun preferenceConflictIsDistinctFromAnOmittedIntake() {
        request("""{"title":"VOICE_CONFIRMATION_DISABLED"}""", 409) { result, _ -> assertEquals("VOICE_CONFIRMATION_DISABLED", (result as AppResult.Failure).code) }
        request("""{"title":"INTAKE_NOT_CONFIRMABLE"}""", 409) { result, _ -> assertEquals("INTAKE_NOT_CONFIRMABLE", (result as AppResult.Failure).code) }
    }

    @Test fun unsupportedSuccessStatusIsRejected() {
        request("""{"status":"SOMETHING_NEW"}""") { result, _ -> assertEquals("INVALID_VOICE_RESULT", (result as AppResult.Failure).code) }
    }

    @Test fun recordingIsDefensivelyCopied() {
        val bytes = byteArrayOf(1, 2)
        val value = ConfirmDoseByVoiceCommand("intake-1", bytes, "audio/mp4", "es-419")
        bytes[0] = 9
        value.audio[1] = 9
        assertArrayEquals(byteArrayOf(1, 2), value.audio)
    }
}
