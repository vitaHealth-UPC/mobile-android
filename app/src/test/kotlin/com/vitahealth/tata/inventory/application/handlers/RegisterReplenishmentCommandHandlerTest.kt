package com.vitahealth.tata.inventory.application.handlers

import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommand
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterReplenishmentCommandHandlerTest {
    private val repository = FakeInventoryRepository()
    private val handler = RegisterReplenishmentCommandHandler(repository)

    @Test
    fun `rejects non-positive quantity without calling the repository`() = runTest {
        val result = handler(RegisterReplenishmentCommand(medicationId = "med-1", quantity = 0))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_QUANTITY", (result as AppResult.Failure).code)
        assertEquals(0, repository.replenishmentCalls)
    }

    @Test
    fun `rejects blank medication reference`() = runTest {
        val result = handler(RegisterReplenishmentCommand(medicationId = "  ", quantity = 10))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_MEDICATION_REFERENCE", (result as AppResult.Failure).code)
        assertEquals(0, repository.replenishmentCalls)
    }

    @Test
    fun `delegates a valid replenishment with trimmed id`() = runTest {
        val result = handler(RegisterReplenishmentCommand(medicationId = " med-1 ", quantity = 30))

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.replenishmentCalls)
        assertEquals("med-1", repository.lastReplenishmentMedicationId)
        assertEquals(30, repository.lastReplenishmentQuantity)
    }
}
