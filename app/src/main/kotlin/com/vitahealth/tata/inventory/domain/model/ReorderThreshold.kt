package com.vitahealth.tata.inventory.domain.model

/**
 * The stock level at or below which a medication is considered low (US-40 / US-42).
 *
 * Cannot be negative. Zero is allowed: it means "only flag as low when the stock runs out".
 */
@JvmInline
value class ReorderThreshold private constructor(val value: Int) {
    companion object {
        fun of(raw: Int): ReorderThreshold? = raw.takeIf { it >= 0 }?.let(::ReorderThreshold)
    }
}
