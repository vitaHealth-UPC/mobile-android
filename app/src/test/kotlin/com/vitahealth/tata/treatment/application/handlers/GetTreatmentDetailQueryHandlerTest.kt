package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQuery
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTreatmentDetailQueryHandlerTest {
    @Test
    fun invalidReferenceIsRejectedBeforeRepositoryCall() = runBlocking {
        val repository = FakeTreatmentDetailRepository()
        val handler = GetTreatmentDetailQueryHandler(repository)

        val result = handler(
            GetTreatmentDetailQuery(
                caregiverId = " ",
                treatmentId = "treatment-1",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_TREATMENT_REFERENCE", (result as AppResult.Failure).code)
        assertFalse(repository.called)
    }

    @Test
    fun validReferenceIsNormalizedAndForwarded() = runBlocking {
        val repository = FakeTreatmentDetailRepository()
        val handler = GetTreatmentDetailQueryHandler(repository)

        val result = handler(
            GetTreatmentDetailQuery(
                caregiverId = " caregiver-1 ",
                treatmentId = " treatment-1 ",
            ),
        )

        assertTrue(result is AppResult.Success)
        assertEquals("caregiver-1", repository.caregiverId)
        assertEquals("treatment-1", repository.treatmentId)
    }

    private class FakeTreatmentDetailRepository : TreatmentDetailRepository {
        var called = false
        var caregiverId: String? = null
        var treatmentId: String? = null

        override suspend fun getTreatmentDetail(
            caregiverId: String,
            treatmentId: String,
        ): AppResult<TreatmentDetailReadModel> {
            called = true
            this.caregiverId = caregiverId
            this.treatmentId = treatmentId
            return AppResult.Success(
                TreatmentDetailReadModel(
                    id = treatmentId,
                    olderAdultId = "adult-1",
                    name = "Control de presión",
                    status = TreatmentStatus.ACTIVE,
                    medicationId = "med-1",
                    dose = "1 comprimido",
                    frequency = "Cada día",
                    scheduledTimes = listOf("08:00", "20:00"),
                    instructions = "Con agua",
                    reminderLeadMinutes = 10,
                ),
            )
        }
    }
}
