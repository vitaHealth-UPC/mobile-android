package com.vitahealth.tata.inventory.application.handlers

import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.commands.RegisterInitialInventoryCommand
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterInitialInventoryCommandHandlerTest {
    private val repository = FakeInventoryRepository()
    private val handler = RegisterInitialInventoryCommandHandler(repository)

    @Test
    fun `rejects non-positive initial quantity without calling the repository`() = runTest {
        val result = handler(
            RegisterInitialInventoryCommand(medicationId = "med-1", initialQuantity = 0, replenishmentThreshold = 5),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_QUANTITY", (result as AppResult.Failure).code)
        assertEquals(0, repository.initialCalls)
    }

    @Test
    fun `rejects negative threshold without calling the repository`() = runTest {
        val result = handler(
            RegisterInitialInventoryCommand(medicationId = "med-1", initialQuantity = 30, replenishmentThreshold = -1),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_THRESHOLD", (result as AppResult.Failure).code)
        assertEquals(0, repository.initialCalls)
    }

    @Test
    fun `delegates a valid initial inventory`() = runTest {
        val result = handler(
            RegisterInitialInventoryCommand(medicationId = "med-1", initialQuantity = 30, replenishmentThreshold = 5),
        )

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.initialCalls)
        assertEquals(30, repository.lastInitialQuantity)
        assertEquals(5, repository.lastInitialThreshold)
    }
}
