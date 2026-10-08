package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQuery
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetDoseDetailQueryHandlerTest {
    private val expected = DoseDetailReadModel(
        id = "intake-1",
        treatmentId = "treatment-1",
        medicationId = "medication-1",
        olderAdultId = "adult-1",
        medicationName = "Losartán 50 mg",
        dose = "1 comprimido",
        instructions = "Con agua",
        scheduledAt = Instant.parse("2026-10-06T13:00:00Z"),
        status = DoseStatus.LATE,
    )

    @Test
    fun returnsDoseDetailWithOperationalState() = runBlocking {
        val handler = GetDoseDetailQueryHandler(FakeRepository(AppResult.Success(expected)))
        val result = handler(GetDoseDetailQuery(" intake-1 "))
        assertTrue(result is AppResult.Success)
        assertEquals(expected, (result as AppResult.Success).value)
    }

    @Test
    fun rejectsBlankIntakeReference() = runBlocking {
        val handler = GetDoseDetailQueryHandler(FakeRepository(AppResult.Success(expected)))
        val result = handler(GetDoseDetailQuery(" "))
        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_INTAKE_REFERENCE", (result as AppResult.Failure).code)
    }

    private class FakeRepository(
        private val result: AppResult<DoseDetailReadModel>,
    ) : DoseDetailRepository {
        override suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel> = result
    }
}
