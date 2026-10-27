package com.vitahealth.tata.inventory.domain.model.valueobjects

/**
 * Normalized nonblank inventory-lot reference.
 *
 * A lot is optional in requests; when present, its length cannot exceed the
 * backend's 200-character contract. Blank input is represented as null by callers.
 */
@JvmInline
value class InventoryLot private constructor(val value: String) {

    companion object {
        const val MAX_LENGTH = 200

        /** Trims a lot and rejects blank or oversized references. */
        fun of(raw: String): InventoryLot? =
            raw.trim()
                .takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH }
                ?.let(::InventoryLot)
    }
}
