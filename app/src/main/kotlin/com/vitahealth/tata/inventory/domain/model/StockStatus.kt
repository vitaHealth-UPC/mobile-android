package com.vitahealth.tata.inventory.domain.model

/**
 * Whether a medication's stock is at or below its replenishment threshold.
 *
 * The backend is the single source of truth for this decision (it exposes a `lowStock`
 * flag computed from remaining stock vs. threshold). This type only carries that decision
 * into the app; it does not recompute the rule from a visual constant.
 */
enum class StockStatus {
    LOW,
    AVAILABLE,
    ;

    companion object {
        fun fromLowStockFlag(lowStock: Boolean): StockStatus =
            if (lowStock) LOW else AVAILABLE
    }
}
