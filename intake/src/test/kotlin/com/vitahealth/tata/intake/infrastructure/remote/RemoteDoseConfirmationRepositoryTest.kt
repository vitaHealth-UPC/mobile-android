package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.domain.model.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.time.Instant

class RemoteDoseConfirmationRepositoryTest {
    private val confirmed = IntakeResponse("intake-1", "treatment-1", "medication-1", "adult-1", "Losartan", "1 tablet", "With water",
        "2026-10-06T13:00:00Z", "CONFIRMED", "2026-10-06T13:02:00Z", "TOUCH")

    @Test fun sendsExactChannelAndDisplaysServerOutcome() = runBlocking {
        val api = FakeApi(Response.success(confirmed.copy(status = "LATE")))
        val result = RemoteDoseConfirmationRepository(api).confirm(ConfirmDoseCommand("intake-1", ConfirmationChannel.VOICE))
        assertEquals("VOICE", api.request?.channel)
        assertEquals("intake-1", api.id)
        val dose = (result as AppResult.Success).value
        assertEquals(DoseStatus.LATE, dose.status)
        assertEquals(Instant.parse("2026-10-06T13:02:00Z"), dose.confirmedAt)
    }

    @Test fun definitiveOmissionIsReturnedAsControlledError() = runBlocking {
        val api = FakeApi(Response.error(409, "{}".toResponseBody()))
        val result = RemoteDoseConfirmationRepository(api).confirm(ConfirmDoseCommand("intake-1", ConfirmationChannel.TOUCH))
        assertEquals("INTAKE_NOT_CONFIRMABLE", (result as AppResult.Failure).code)
    }

    @Test fun networkFailureDoesNotFabricateConfirmation() = runBlocking {
        val api = FakeApi(Response.success(confirmed), java.io.IOException("offline"))
        val result = RemoteDoseConfirmationRepository(api).confirm(ConfirmDoseCommand("intake-1", ConfirmationChannel.TOUCH))
        assertEquals("NETWORK_UNAVAILABLE", (result as AppResult.Failure).code)
    }

    @Test fun cancellationIsPropagated() = runBlocking {
        val api = FakeApi(Response.success(confirmed), CancellationException("cancelled"))
        try {
            RemoteDoseConfirmationRepository(api).confirm(ConfirmDoseCommand("intake-1", ConfirmationChannel.TOUCH))
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }

    private class FakeApi(val response: Response<IntakeResponse>, val failure: Exception? = null) : IntakeApiService {
        override suspend fun getAgenda(olderAdultId: String, from: String, to: String): Response<List<IntakeResponse>> = error("Unused")
        var request: ConfirmIntakeRequest? = null
        var id: String? = null
        override suspend fun confirmDose(intakeId: String, request: ConfirmIntakeRequest): Response<IntakeResponse> {
            this.id = intakeId
            this.request = request
            failure?.let { throw it }
            return response
        }
        override suspend fun getDoseDetail(intakeId: String) = response
        override suspend fun getNextDose(olderAdultId: String) = response
    }
}
