package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.NextDoseRepository
import com.vitahealth.tata.intake.application.queries.GetNextDoseQuery
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNextDoseQueryHandlerTest {
    @Test
    fun returnsNearestDoseProvidedByRepository() = runBlocking {
        val expected = NextDoseReadModel(
            id = "dose-1",
            treatmentId = "treatment-1",
            medicationId = "medication-1",
            olderAdultId = "adult-1",
            medicationName = "Losartán 50 mg",
            dose = "1 comprimido",
            instructions = "Con agua",
            scheduledAt = Instant.parse("2026-10-06T13:00:00Z"),
            status = DoseStatus.PENDING,
        )
        val handler = GetNextDoseQueryHandler(FakeRepository(AppResult.Success(expected)))

        val result = handler(GetNextDoseQuery(" adult-1 "))

        assertTrue(result is AppResult.Success)
        assertEquals(expected, (result as AppResult.Success).value)
    }

    @Test
    fun returnsNoNextDoseWhenRepositoryHasNoPendingDose() = runBlocking {
        val handler = GetNextDoseQueryHandler(FakeRepository(AppResult.Success(null)))

        val result = handler(GetNextDoseQuery("adult-1"))

        assertTrue(result is AppResult.Success)
        assertNull((result as AppResult.Success).value)
    }

    @Test
    fun rejectsBlankOlderAdultReference() = runBlocking {
        val handler = GetNextDoseQueryHandler(FakeRepository(AppResult.Success(null)))

        val result = handler(GetNextDoseQuery(" "))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_OLDER_ADULT_REFERENCE", (result as AppResult.Failure).code)
    }

    private class FakeRepository(
        private val result: AppResult<NextDoseReadModel?>,
    ) : NextDoseRepository {
        override suspend fun getNextDose(olderAdultId: String): AppResult<NextDoseReadModel?> = result
    }
}
