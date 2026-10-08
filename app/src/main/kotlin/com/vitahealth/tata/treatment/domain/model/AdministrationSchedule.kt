package com.vitahealth.tata.treatment.domain.model

import java.time.LocalTime

data class AdministrationSchedule private constructor(
    val times: List<LocalTime>,
) {
    companion object {
        fun create(times: List<LocalTime>): AdministrationSchedule? {
            val normalized = times.distinct().sorted()
            return normalized
                .takeIf { it.isNotEmpty() }
                ?.let(::AdministrationSchedule)
        }
    }
}
