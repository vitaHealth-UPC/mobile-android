package com.vitahealth.tata.monitoring.application

import com.vitahealth.tata.monitoring.application.commands.CreatePersonalNoteCommand
import com.vitahealth.tata.monitoring.application.commands.CreatePersonalNoteCommandHandler
import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.queries.GetFamilyHistoryQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilyHistoryQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQuery
import com.vitahealth.tata.monitoring.application.queries.GetFamilySummaryQueryHandler
import com.vitahealth.tata.monitoring.application.queries.GetPersonalNotesQuery
import com.vitahealth.tata.monitoring.application.queries.GetPersonalNotesQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies request validation before crossing account-scoped repository boundaries. */
class MonitoringCqrsBoundaryTest {

    private class FamilyRepository : FamilyMonitoringRepository {
        var summaries = 0
        var histories = 0

        override suspend fun summary(
            caregiverId: String,
            olderAdultId: String,
            name: String,
            day: LocalDate,
            zone: ZoneId,
        ): AppResult<FamilySummary> {
            summaries++
            return AppResult.Failure("Not present", code = "NOT_FOUND")
        }

        override suspend fun history(
            caregiverId: String,
            olderAdultId: String,
        ): AppResult<List<IntakeHistoryEntry>> {
            histories++
            return AppResult.Success(emptyList())
        }
    }

    private class PersonalRepository : PersonalNotesRepository {
        var reads = 0
        var writes = 0

        override suspend fun list(): AppResult<List<PersonalNote>> {
            reads++
            return AppResult.Success(emptyList())
        }

        override suspend fun create(
            title: String,
            text: String,
            category: PersonalNoteCategory,
        ): AppResult<PersonalNote> {
            writes++
            return AppResult.Success(
                PersonalNote(1L, title, text, category, Instant.EPOCH)
            )
        }
    }

    @Test
    fun summaryAndHistoryRejectMissingCaregiverBeforeAccess() = runTest {
        val repo = FamilyRepository()
        val summary =
            GetFamilySummaryQueryHandler(repo)(
                GetFamilySummaryQuery(
                    " ", "adult-1", "Ana",
                    LocalDate.of(2026, 10, 24),
                    ZoneId.of("America/Lima"),
                )
            )
        val history =
            GetFamilyHistoryQueryHandler(repo)(
                GetFamilyHistoryQuery("caregiver-1", " ")
            )
        assertTrue(summary is AppResult.Failure)
        assertTrue(history is AppResult.Failure)
        assertEquals(0, repo.summaries)
        assertEquals(0, repo.histories)
    }

    @Test
    fun validFamilyHistoryUsesReadOnlyQueryHandler() = runTest {
        val repo = FamilyRepository()
        val result =
            GetFamilyHistoryQueryHandler(repo)(
                GetFamilyHistoryQuery(" caregiver-1 ", " adult-1 ")
            )
        assertTrue(result is AppResult.Success)
        assertEquals(1, repo.histories)
        assertEquals(0, repo.summaries)
    }

    @Test
    fun personalNotesRejectInvalidInputAndDistinguishReadsFromWrites() = runTest {
        val repo = PersonalRepository()
        val list = GetPersonalNotesQueryHandler(repo)
        val create = CreatePersonalNoteCommandHandler(repo)
        val missingTitle =
            create(CreatePersonalNoteCommand(" ", "Text", PersonalNoteCategory.ROUTINE))
        assertTrue(missingTitle is AppResult.Failure)
        assertEquals(0, repo.writes)
        assertTrue(list(GetPersonalNotesQuery) is AppResult.Success)
        assertEquals(1, repo.reads)
        val saved =
            create(CreatePersonalNoteCommand(" Agua ", " Beber agua ", PersonalNoteCategory.ROUTINE))
        assertTrue(saved is AppResult.Success)
        assertEquals("Agua", (saved as AppResult.Success).value.title)
        assertEquals(1, repo.writes)
    }
}
