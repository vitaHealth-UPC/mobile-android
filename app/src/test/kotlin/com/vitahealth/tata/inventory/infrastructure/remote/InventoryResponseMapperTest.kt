package com.vitahealth.tata.inventory.infrastructure.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Guarantees that invalid transport data cannot bypass domain invariants when
 * a stock projection is constructed.
 */
class InventoryResponseMapperTest {

    @Test
    fun acceptsAnOptionalLotWithinTheDomainLimit() {
        val stock = response(batchLot = "LOT-2026").toReadModel("med-1")

        assertEquals("LOT-2026", stock.batches.single().lot)
    }

    @Test
    fun rejectsBlankAndOversizedLotMetadataFromTheBackend() {
        for (lot in listOf("   ", "x".repeat(201))) {
            assertThrows(IllegalArgumentException::class.java) {
                response(batchLot = lot).toReadModel("med-1")
            }
        }
    }

    private fun response(batchLot: String?): InventoryResponse =
        InventoryResponse(
            id = "inventory-1",
            medicationId = "med-1",
            remainingStock = 5,
            replenishmentThreshold = 5,
            lowStock = true,
            batches =
                listOf(
                    BatchResponse(
                        id = "batch-1",
                        quantity = 5,
                        registeredAt = "2026-10-01T10:00:00Z",
                        lot = batchLot,
                    )
                ),
            createdAt = "2026-10-01T10:00:00Z",
            updatedAt = "2026-10-01T10:00:00Z",
        )
}
