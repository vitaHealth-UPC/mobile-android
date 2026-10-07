package com.vitahealth.tata.inventory.domain.model

/**
 * A number of medication units that must be strictly positive.
 *
 * Used for the initial stock (US-40) and each replenishment (US-43). An invalid quantity
 * is a validation outcome, not a separate screen: callers get `null` and surface the error.
 */
@JvmInline
value class Quantity private constructor(val value: Int) {
    companion object {
        fun of(raw: Int): Quantity? = raw.takeIf { it > 0 }?.let(::Quantity)
    }
}
