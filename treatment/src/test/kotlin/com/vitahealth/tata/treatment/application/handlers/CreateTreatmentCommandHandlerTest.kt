package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.application.commands.CreateTreatmentCommand
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.treatment.domain.model.Treatment
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTreatmentCommandHandlerTest {
    @Test
    fun blankNameIsRejectedBeforeRepositoryCall() = runBlocking {
        val repository = FakeTreatmentRepository()
        val handler = CreateTreatmentCommandHandler(repository)

        val result = handler(
            CreateTreatmentCommand(
                caregiverId = "caregiver-1",
                olderAdultId = "adult-1",
                name = " ",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("REQUIRED_FIELDS_MISSING", (result as AppResult.Failure).code)
        assertFalse(repository.called)
    }

    @Test
    fun validTreatmentIsNormalizedAndCreatedAsDraft() = runBlocking {
        val repository = FakeTreatmentRepository()
        val handler = CreateTreatmentCommandHandler(repository)

        val result = handler(
            CreateTreatmentCommand(
                caregiverId = " caregiver-1 ",
                olderAdultId = " adult-1 ",
                name = " Control de presión ",
            ),
        )

        assertTrue(result is AppResult.Success)
        val treatment = (result as AppResult.Success).value
        assertEquals(TreatmentStatus.DRAFT, treatment.status)
        assertEquals("caregiver-1", repository.caregiverId)
        assertEquals("adult-1", repository.olderAdultId)
        assertEquals("Control de presión", repository.name)
    }

    @Test
    fun unexpectedInitialStateIsRejected() = runBlocking {
        val repository = FakeTreatmentRepository(status = TreatmentStatus.ACTIVE)
        val handler = CreateTreatmentCommandHandler(repository)

        val result = handler(
            CreateTreatmentCommand(
                caregiverId = "caregiver-1",
                olderAdultId = "adult-1",
                name = "Control de presión",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_INITIAL_TREATMENT_STATE", (result as AppResult.Failure).code)
    }

    private class FakeTreatmentRepository(
        private val status: TreatmentStatus = TreatmentStatus.DRAFT,
    ) : TreatmentRepository {
        var called = false
        var caregiverId: String? = null
        var olderAdultId: String? = null
        var name: String? = null

        override suspend fun registerMedication(
            caregiverId: String,
            olderAdultId: String,
            name: String,
            presentation: String,
        ): AppResult<Medication> = error("not used")

        override suspend fun createTreatment(
            caregiverId: String,
            olderAdultId: String,
            name: String,
        ): AppResult<Treatment> {
            called = true
            this.caregiverId = caregiverId
            this.olderAdultId = olderAdultId
            this.name = name
            return AppResult.Success(
                Treatment(
                    id = "treatment-1",
                    olderAdultId = olderAdultId,
                    name = name,
                    status = status,
                ),
            )
        }
    }
}
