package com.vitahealth.tata.inventory.application.commands

import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommand
import com.vitahealth.tata.inventory.application.commands.RegisterReplenishmentCommandHandler
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterReplenishmentCommandHandlerTest {
    @Test
    fun lotLengthIsCheckedAfterNormalizationLikeTheBackend() = runTest {
        val repository = FakeInventoryRepository()
        val handler = RegisterReplenishmentCommandHandler(repository)
        val lot = "A".repeat(200)

        val result = handler(RegisterReplenishmentCommand(" med-1 ", 30, "  $lot  "))

        assertTrue(result is AppResult.Success)
        assertEquals("med-1", repository.lastReplenishmentMedicationId)
        assertEquals(lot, repository.lastReplenishmentLot)
    }

    @Test
    fun blankLotIsAbsentAndAnOversizedLotDoesNotReachTheRepository() = runTest {
        val repository = FakeInventoryRepository()
        val handler = RegisterReplenishmentCommandHandler(repository)

        assertTrue(handler(RegisterReplenishmentCommand("med-1", 30, "   ")) is AppResult.Success)
        assertNull(repository.lastReplenishmentLot)
        val result = handler(RegisterReplenishmentCommand("med-1", 30, "A".repeat(201)))

        assertEquals("INVALID_LOT", (result as AppResult.Failure).code)
        assertEquals(1, repository.replenishmentCalls)
    }

    @Test
    fun invalidMedicationAndQuantityDoNotReachTheRepository() = runTest {
        val repository = FakeInventoryRepository()
        val handler = RegisterReplenishmentCommandHandler(repository)

        val missingMedication = handler(RegisterReplenishmentCommand(" ", 30))
        val invalidQuantity = handler(RegisterReplenishmentCommand("med-1", 0))

        assertEquals("INVALID_MEDICATION_REFERENCE", (missingMedication as AppResult.Failure).code)
        assertEquals("INVALID_QUANTITY", (invalidQuantity as AppResult.Failure).code)
        assertEquals(0, repository.replenishmentCalls)
    }
}
