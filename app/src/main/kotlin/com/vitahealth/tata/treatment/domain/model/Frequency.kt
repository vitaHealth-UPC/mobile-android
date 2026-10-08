package com.vitahealth.tata.treatment.domain.model

@JvmInline
value class Frequency private constructor(val value: String) {
    companion object {
        fun parse(raw: String): Frequency? {
            val normalized = raw.trim()
            return normalized
                .takeIf { it.isNotBlank() && it.length <= 100 }
                ?.let(::Frequency)
        }
    }
}
