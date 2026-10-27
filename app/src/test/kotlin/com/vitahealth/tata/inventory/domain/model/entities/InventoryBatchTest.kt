package com.vitahealth.tata.inventory.domain.model.entities

import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Verifies batch identity and positive units on both construction and copying. */
class InventoryBatchTest {
    @Test
    fun invalidIdentityAndQuantitiesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { InventoryBatch(" ", 1, Instant.EPOCH) }
        for (quantity in listOf(0, -1, Int.MIN_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) {
                InventoryBatch("batch", quantity, Instant.EPOCH)
            }
        }
    }

    @Test
    fun invalidLotMetadataCannotEnterTheDomainOrBypassCopyValidation() {
        val valid = InventoryBatch("batch", 1, Instant.EPOCH, "LOT-1")
        val overlong = "L".repeat(201)

        assertThrows(IllegalArgumentException::class.java) {
            InventoryBatch("batch", 1, Instant.EPOCH, "  ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            InventoryBatch("batch", 1, Instant.EPOCH, overlong)
        }
        assertThrows(IllegalArgumentException::class.java) {
            valid.copy(lot = overlong)
        }
        assertEquals("LOT-1", valid.lot)
    }

    @Test
    fun copyingCannotBypassBatchInvariants() {
        val batch = InventoryBatch("batch", 1, Instant.EPOCH, "lot")
        assertThrows(IllegalArgumentException::class.java) { batch.copy(quantity = 0) }
        assertThrows(IllegalArgumentException::class.java) { batch.copy(id = "") }
        assertEquals(Int.MAX_VALUE, batch.copy(quantity = Int.MAX_VALUE).quantity)
    }
}
