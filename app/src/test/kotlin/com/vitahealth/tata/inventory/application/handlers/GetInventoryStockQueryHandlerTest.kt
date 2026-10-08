package com.vitahealth.tata.inventory.application.handlers

import com.vitahealth.tata.inventory.application.FakeInventoryRepository
import com.vitahealth.tata.inventory.application.queries.GetInventoryStockQuery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetInventoryStockQueryHandlerTest {
    private val repository = FakeInventoryRepository()
    private val handler = GetInventoryStockQueryHandler(repository)

    @Test
    fun `rejects blank medication reference`() = runTest {
        val result = handler(GetInventoryStockQuery(medicationId = " "))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_MEDICATION_REFERENCE", (result as AppResult.Failure).code)
        assertEquals(0, repository.getStockCalls)
    }

    @Test
    fun `delegates and returns the repository result`() = runTest {
        val result = handler(GetInventoryStockQuery(medicationId = "med-1"))

        assertTrue(result is AppResult.Success)
        assertEquals(1, repository.getStockCalls)
        assertEquals("med-1", repository.lastGetStockMedicationId)
    }

    @Test
    fun `passes through a not-found failure unchanged`() = runTest {
        repository.stockResult = AppResult.Failure(message = "inventory not found", code = "INVENTORY_NOT_FOUND")

        val result = handler(GetInventoryStockQuery(medicationId = "med-1"))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVENTORY_NOT_FOUND", (result as AppResult.Failure).code)
    }
}
