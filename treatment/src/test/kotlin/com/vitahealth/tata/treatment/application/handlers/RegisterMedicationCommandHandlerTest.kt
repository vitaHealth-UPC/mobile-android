package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.TreatmentRepository
import com.vitahealth.tata.treatment.application.commands.RegisterMedicationCommand
import com.vitahealth.tata.treatment.domain.model.Medication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterMedicationCommandHandlerTest {
    @Test
    fun missingRequiredDataIsRejectedBeforeCallingRepository() = runBlocking {
        val repository = FakeTreatmentRepository()
        val handler = RegisterMedicationCommandHandler(repository)

        val result = handler(
            RegisterMedicationCommand(
                caregiverId = "caregiver-1",
                olderAdultId = "adult-1",
                name = " ",
                presentation = "50 mg",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("REQUIRED_FIELDS_MISSING", (result as AppResult.Failure).code)
        assertFalse(repository.called)
    }

    @Test
    fun validMedicationIsNormalizedAndForwarded() = runBlocking {
        val repository = FakeTreatmentRepository()
        val handler = RegisterMedicationCommandHandler(repository)

        val result = handler(
            RegisterMedicationCommand(
                caregiverId = " caregiver-1 ",
                olderAdultId = " adult-1 ",
                name = " Losartán ",
                presentation = " 50 mg, comprimido ",
            ),
        )

        assertTrue(result is AppResult.Success)
        assertEquals("caregiver-1", repository.caregiverId)
        assertEquals("adult-1", repository.olderAdultId)
        assertEquals("Losartán", repository.name)
        assertEquals("50 mg, comprimido", repository.presentation)
    }

    private class FakeTreatmentRepository : TreatmentRepository {
        var called = false
        var caregiverId: String? = null
        var olderAdultId: String? = null
        var name: String? = null
        var presentation: String? = null

        override suspend fun registerMedication(
            caregiverId: String,
            olderAdultId: String,
            name: String,
            presentation: String,
        ): AppResult<Medication> {
            called = true
            this.caregiverId = caregiverId
            this.olderAdultId = olderAdultId
            this.name = name
            this.presentation = presentation
            return AppResult.Success(
                Medication(
                    id = "med-1",
                    olderAdultId = olderAdultId,
                    name = name,
                    presentation = presentation,
                    active = true,
                ),
            )
        }
    }
}
