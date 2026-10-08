package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.treatment.application.commands.DeactivateMedicationCommand
import com.vitahealth.tata.treatment.application.commands.UpdateMedicationCommand
import com.vitahealth.tata.treatment.application.queries.ListMedicationsQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationManagementHandlersTest {
    private val repository = FakeMedicationManagementRepository(listOf(losartan, metformin))
    private val list = ListMedicationsQueryHandler(repository)
    private val update = UpdateMedicationCommandHandler(repository)
    private val deactivate = DeactivateMedicationCommandHandler(repository)

    @Test
    fun listingWithoutAnOlderAdultIsRejectedBeforeCallingTheBackend() = runBlocking {
        val result = list(ListMedicationsQuery(caregiverId = "caregiver-1", olderAdultId = " "))

        assertEquals("INVALID_OLDER_ADULT_REFERENCE", (result as AppResult.Failure).code)
        assertEquals(0, repository.listCalls)
    }

    @Test
    fun listingReturnsEveryMedicationIncludingTheInactiveOnes() = runBlocking {
        val result = list(ListMedicationsQuery(" caregiver-1 ", " adult-1 "))

        assertEquals(listOf(losartan, metformin), (result as AppResult.Success).value)
    }

    @Test
    fun updatingWithBlankFieldsIsRejectedBeforeCallingTheBackend() = runBlocking {
        val noName = update(UpdateMedicationCommand("caregiver-1", "med-1", " ", "50 mg"))
        val noDose = update(UpdateMedicationCommand("caregiver-1", "med-1", "Losartán", ""))

        assertEquals("REQUIRED_FIELDS_MISSING", (noName as AppResult.Failure).code)
        assertEquals("REQUIRED_FIELDS_MISSING", (noDose as AppResult.Failure).code)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun updatingWithoutAMedicationReferenceIsRejected() = runBlocking {
        val result = update(UpdateMedicationCommand("caregiver-1", " ", "Losartán", "50 mg"))

        assertEquals("INVALID_MEDICATION_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.updates.isEmpty())
    }

    @Test
    fun updatingTrimsTheValuesBeforeSendingThem() = runBlocking {
        val result = update(UpdateMedicationCommand(" caregiver-1 ", " med-1 ", " Losartán 100 ", " 100 mg "))

        assertTrue(result is AppResult.Success)
        assertEquals(Triple("med-1", "Losartán 100", "100 mg"), repository.updates.single())
    }

    @Test
    fun deactivatingKeepsTheMedicationButMarksItInactive() = runBlocking {
        val result = deactivate(DeactivateMedicationCommand("caregiver-1", "med-1"))

        assertFalse((result as AppResult.Success).value.active)
        assertEquals(listOf("med-1"), repository.deactivations)
        assertEquals(2, repository.medications.size)
    }

    @Test
    fun deactivatingWithoutAMedicationReferenceIsRejected() = runBlocking {
        val result = deactivate(DeactivateMedicationCommand("", "med-1"))

        assertEquals("INVALID_MEDICATION_REFERENCE", (result as AppResult.Failure).code)
        assertTrue(repository.deactivations.isEmpty())
    }
}
