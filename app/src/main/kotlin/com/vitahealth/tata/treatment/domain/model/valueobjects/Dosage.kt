package com.vitahealth.tata.treatment.domain.model.valueobjects

/** Normalized dose description with the treatment text-length constraint. */
@JvmInline
value class Dosage private constructor(val value: String) {
    companion object {
        fun parse(raw: String): Dosage? {
            val normalized = raw.trim()
            return normalized.takeIf { it.isNotBlank() && it.length <= 100 }?.let(::Dosage)
        }
    }
}
