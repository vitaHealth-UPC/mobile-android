package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.presentation.agenda.IntakeAgendaUiState
import com.vitahealth.tata.intake.application.queries.AgendaWeek
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.time.*

class RemoteIntakeAgendaRepositoryTest {
    private val from = Instant.parse("2026-10-05T05:00:00Z")
    private val to = from.plusSeconds(7 * 86400)
    private fun dose(id: String, time: String, status: String = "PENDING") = IntakeResponse(id, "treatment", "medication", "adult", "Losartan", "1 tablet", null, time, status)
    @Test fun sendsExactUtcBoundsAndKeepsChronologicalServerOutcomes() = runBlocking {
        val api = FakeApi(listOf(dose("second", "2026-10-06T13:00:00Z"), dose("first", "2026-10-06T04:30:00Z", "OMITTED")))
        val result = RemoteIntakeAgendaRepository(api).getAgenda("adult", from, to) as AppResult.Success
        assertEquals(listOf("adult", from.toString(), to.toString()), api.arguments)
        assertEquals(listOf("first", "second"), result.value.map { it.id })
        assertEquals(DoseStatus.OMITTED, result.value.first().status)
        val state = IntakeAgendaUiState(AgendaWeek.containing(LocalDate.of(2026, 10, 6), ZoneId.of("America/Bogota")), LocalDate.of(2026, 10, 5), result.value, false)
        assertEquals(listOf("first"), state.selectedDoses.map { it.id })
    }
    @Test fun emptyResponseIsAnEmptyAgenda() = runBlocking {
        val result = RemoteIntakeAgendaRepository(FakeApi(emptyList())).getAgenda("adult", from, to) as AppResult.Success
        assertTrue(result.value.isEmpty())
    }
    @Test fun malformedStateDoesNotHideIncompleteAgendaAsSuccess() = runBlocking {
        val result = RemoteIntakeAgendaRepository(FakeApi(listOf(dose("first", from.toString(), "UNKNOWN")))).getAgenda("adult", from, to)
        assertEquals("INVALID_DOSE_STATUS", (result as AppResult.Failure).code)
    }
    @Test fun offlineIsAnErrorAndCancellationPropagates() = runBlocking {
        val result = RemoteIntakeAgendaRepository(FakeApi(emptyList(), java.io.IOException())).getAgenda("adult", from, to)
        assertEquals("NETWORK_UNAVAILABLE", (result as AppResult.Failure).code)
        try {
            RemoteIntakeAgendaRepository(FakeApi(emptyList(), CancellationException())).getAgenda("adult", from, to)
            fail("Must propagate cancellation")
        } catch (_: CancellationException) { }
    }
    private class FakeApi(val doses: List<IntakeResponse>, val failure: Exception? = null) : IntakeApiService {
        var arguments: List<String>? = null
        override suspend fun getAgenda(olderAdultId: String, from: String, to: String): Response<List<IntakeResponse>> {
            arguments = listOf(olderAdultId, from, to)
            failure?.let { throw it }
            return Response.success(doses)
        }
        override suspend fun confirmDose(intakeId: String, request: ConfirmIntakeRequest): Response<IntakeResponse> = error("Unused")
        override suspend fun getNextDose(olderAdultId: String): Response<IntakeResponse> = error("Unused")
        override suspend fun getDoseDetail(intakeId: String): Response<IntakeResponse> = error("Unused")
    }
}
