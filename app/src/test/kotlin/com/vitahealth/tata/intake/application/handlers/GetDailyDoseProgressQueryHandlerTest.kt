package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.queries.GetDailyDoseProgressQuery
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class GetDailyDoseProgressQueryHandlerTest {
    private val query = GetDailyDoseProgressQuery(" adult-1 ", LocalDate.parse("2026-10-07"), ZoneId.of("America/Bogota"))
    private fun dose(id: String, status: DoseStatus, at: String = "2026-10-07T13:00:00Z", adult: String = "adult-1") =
        DoseDetailReadModel(id, "treatment", "medication", adult, "Medication", "1", "", Instant.parse(at), status)

    @Test fun countsUniqueAdultDosesInsideTheLocalDay() = runBlocking {
        val confirmed = dose("1", DoseStatus.CONFIRMED)
        val repository = object : IntakeAgendaRepository {
            override suspend fun getAgenda(olderAdultId: String, from: Instant, to: Instant): AppResult<List<DoseDetailReadModel>> {
                assertEquals("adult-1", olderAdultId)
                assertEquals(Instant.parse("2026-10-07T05:00:00Z"), from)
                assertEquals(Instant.parse("2026-10-08T05:00:00Z"), to)
                return AppResult.Success(listOf(confirmed, confirmed, dose("2", DoseStatus.LATE),
                    dose("3", DoseStatus.PENDING), dose("4", DoseStatus.OMITTED),
                    dose("5", DoseStatus.CONFIRMED, "2026-10-08T05:00:00Z"),
                    dose("6", DoseStatus.CONFIRMED, adult = "another-adult")))
            }
        }
        val result = GetDailyDoseProgressQueryHandler(repository)(query) as AppResult.Success
        assertEquals(2, result.value.completed)
        assertEquals(4, result.value.total)
    }

    @Test fun propagatesFailureInsteadOfReportingZeroProgress() = runBlocking {
        val failure = AppResult.Failure("Offline", code = "NETWORK_ERROR")
        val repository = object : IntakeAgendaRepository {
            override suspend fun getAgenda(olderAdultId: String, from: Instant, to: Instant) = failure
        }
        assertEquals(failure, GetDailyDoseProgressQueryHandler(repository)(query))
    }

    @Test fun rejectsMissingAdultBeforeQuerying() = runBlocking {
        val repository = object : IntakeAgendaRepository {
            override suspend fun getAgenda(olderAdultId: String, from: Instant, to: Instant): AppResult<List<DoseDetailReadModel>> = error("Must not query")
        }
        val result = GetDailyDoseProgressQueryHandler(repository)(query.copy(olderAdultId = " ")) as AppResult.Failure
        assertEquals("INVALID_OLDER_ADULT_REFERENCE", result.code)
    }
}
