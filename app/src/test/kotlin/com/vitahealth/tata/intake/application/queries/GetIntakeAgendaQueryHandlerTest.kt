package com.vitahealth.tata.intake.application.queries

import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validates the agenda query without a database, network or Compose dependency. */
class GetIntakeAgendaQueryHandlerTest {

    private val from = Instant.parse("2026-10-19T05:00:00Z")
    private val to = Instant.parse("2026-10-26T05:00:00Z")

    private class RecordingRepository : IntakeAgendaRepository {
        val calls = mutableListOf<Triple<String, Instant, Instant>>()

        override suspend fun getAgenda(
            olderAdultId: String,
            from: Instant,
            to: Instant,
        ): AppResult<List<DoseDetailReadModel>> {
            calls.add(Triple(olderAdultId, from, to))
            return AppResult.Success(emptyList())
        }
    }

    @Test
    fun blankOwnerDoesNotReadFromAnAccountScopedRepository() = runTest {
        val repository = RecordingRepository()
        val result =
            GetIntakeAgendaQueryHandler(repository)(GetIntakeAgendaQuery(" ", from, to))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_OLDER_ADULT_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun invertedOrEmptyTimeWindowNeverCallsTheRepository() = runTest {
        val repository = RecordingRepository()
        val handler = GetIntakeAgendaQueryHandler(repository)

        assertTrue(handler(GetIntakeAgendaQuery("adult-1", to, from)) is AppResult.Failure)
        assertTrue(handler(GetIntakeAgendaQuery("adult-1", from, from)) is AppResult.Failure)
        assertTrue(repository.calls.isEmpty())
    }

    @Test
    fun validQueryPreservesAbsoluteWindowAndNormalizesOwnerReference() = runTest {
        val repository = RecordingRepository()
        val result =
            GetIntakeAgendaQueryHandler(repository)(
                GetIntakeAgendaQuery(" adult-1 ", from, to)
            )

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(Triple("adult-1", from, to)), repository.calls)
    }
}
