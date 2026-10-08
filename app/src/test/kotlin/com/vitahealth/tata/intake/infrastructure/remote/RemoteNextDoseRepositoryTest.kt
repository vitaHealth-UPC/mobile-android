package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RemoteNextDoseRepositoryTest {
    @Test
    fun cancellationPropagatesInsteadOfBecomingANetworkError() = runBlocking {
        val cancellation = CancellationException("Screen closed")
        try {
            RemoteNextDoseRepository(FailingApi(cancellation)).getNextDose("adult")
            fail("Expected cancellation")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }

    @Test
    fun connectionFailureStillProducesTheOfflineState() = runBlocking {
        val result = RemoteNextDoseRepository(FailingApi(IOException())).getNextDose("adult")
        assertEquals("NETWORK_UNAVAILABLE", (result as AppResult.Failure).code)
    }

    private class FailingApi(private val failure: Exception) : IntakeApiService {
        override suspend fun getNextDose(olderAdultId: String): Response<IntakeResponse> = throw failure
        override suspend fun getAgenda(olderAdultId: String, from: String, to: String): Response<List<IntakeResponse>> = error("Unused")
        override suspend fun confirmDose(intakeId: String, request: ConfirmIntakeRequest): Response<IntakeResponse> = error("Unused")
        override suspend fun getDoseDetail(intakeId: String): Response<IntakeResponse> = error("Unused")
    }
}
