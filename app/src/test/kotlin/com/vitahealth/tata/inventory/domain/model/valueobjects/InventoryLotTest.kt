package com.vitahealth.tata.inventory.domain.model.valueobjects

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Regression tests for the optional inventory lot invariant. */
class InventoryLotTest {

    @Test
    fun trimsAndAcceptsTheMaximumValidReference() {
        val lot = "A".repeat(InventoryLot.MAX_LENGTH)
        assertEquals(lot, InventoryLot.of("  $lot  ")?.value)
    }

    @Test
    fun rejectsBlankAndOversizedReferences() {
        assertNull(InventoryLot.of("  "))
        assertNull(InventoryLot.of("A".repeat(InventoryLot.MAX_LENGTH + 1)))
    }
}
