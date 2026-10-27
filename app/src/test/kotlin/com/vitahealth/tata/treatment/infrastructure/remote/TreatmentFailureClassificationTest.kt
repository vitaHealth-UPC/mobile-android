package com.vitahealth.tata.treatment.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class TreatmentFailureClassificationTest {
    @Test
    fun connectionFailureIsDifferentFromAnInvalidResponse() = runBlocking {
        val network = IOException("No network")
        val malformed = IllegalArgumentException("Invalid payload")

        for ((error, code) in
            listOf(
                network to "NETWORK_UNAVAILABLE",
                malformed to "INVALID_RESPONSE",
                MalformedJsonException("Invalid JSON") to "INVALID_RESPONSE",
            )) {
            val result =
                repository(error).registerMedication("caregiver", "adult", "Losartán", "Comprimido")
            assertEquals(code, (result as AppResult.Failure).code)
            assertSame(error, result.cause)
        }
    }

    @Test
    fun unauthorizedHttpResponseHasASessionFailureCode() {
        assertEquals("SESSION_REQUIRED", treatmentCode(401))
        assertEquals("CARE_LINK_NOT_AUTHORIZED", treatmentCode(403))
    }

    @Test
    fun cancellationStillPropagatesToTheScreen() {
        val cancellation = CancellationException("Closed")
        val thrown =
            assertThrows(CancellationException::class.java) {
                runBlocking {
                    repository(cancellation)
                        .registerMedication("caregiver", "adult", "Losartán", "Comprimido")
                }
            }
        assertSame(cancellation, thrown)
    }

    private fun repository(error: Exception): RemoteTreatmentRepository {
        val api =
            Proxy.newProxyInstance(
                TreatmentApiService::class.java.classLoader,
                arrayOf(TreatmentApiService::class.java),
            ) { _, _, arguments ->
                // Deliver the failure through the suspend contract, without Java proxy exception
                // wrapping.
                @Suppress("UNCHECKED_CAST")
                val continuation = arguments.last() as Continuation<Any?>
                continuation.resumeWith(Result.failure(error))
                COROUTINE_SUSPENDED
            } as TreatmentApiService
        return RemoteTreatmentRepository(api)
    }
}
