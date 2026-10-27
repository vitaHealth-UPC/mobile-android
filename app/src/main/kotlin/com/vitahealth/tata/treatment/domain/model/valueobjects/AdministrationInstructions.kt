package com.vitahealth.tata.treatment.domain.model.valueobjects

/** Optional administration instructions limited to 500 normalized characters. */
@JvmInline
value class AdministrationInstructions private constructor(val value: String) {
    companion object {
        fun parse(raw: String): AdministrationInstructions? {
            val normalized = raw.trim()
            return normalized.takeIf { it.length <= 500 }?.let(::AdministrationInstructions)
        }
    }
}
