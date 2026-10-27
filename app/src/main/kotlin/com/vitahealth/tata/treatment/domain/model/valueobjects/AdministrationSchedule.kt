package com.vitahealth.tata.treatment.domain.model.valueobjects

import java.time.LocalTime
import java.util.Collections

/** Nonempty, sorted and distinct local administration times. */
class AdministrationSchedule private constructor(times: List<LocalTime>) {
    val times: List<LocalTime> = Collections.unmodifiableList(times.toList())

    override fun equals(other: Any?): Boolean =
        other is AdministrationSchedule && times == other.times

    override fun hashCode(): Int = times.hashCode()

    override fun toString(): String = "AdministrationSchedule(times=$times)"

    companion object {
        fun create(times: List<LocalTime>): AdministrationSchedule? {
            val normalized = times.distinct().sorted()
            return normalized.takeIf { it.isNotEmpty() }?.let(::AdministrationSchedule)
        }
    }
}
