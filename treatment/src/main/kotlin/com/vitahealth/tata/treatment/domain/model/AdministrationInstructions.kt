package com.vitahealth.tata.treatment.domain.model

@JvmInline
value class AdministrationInstructions private constructor(
    val value: String,
) {
    companion object {
        fun parse(raw: String): AdministrationInstructions? {
            val normalized = raw.trim()
            return normalized
                .takeIf { it.length <= 500 }
                ?.let(::AdministrationInstructions)
        }
    }
}
