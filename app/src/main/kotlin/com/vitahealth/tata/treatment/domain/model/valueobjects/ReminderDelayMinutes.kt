package com.vitahealth.tata.treatment.domain.model.valueobjects

/** Nonnegative interval in minutes before a follow-up reminder, limited to one day. */
@JvmInline
value class ReminderDelayMinutes private constructor(val value: Int) {
    companion object {
        fun parse(raw: String): ReminderDelayMinutes? =
            raw.trim().toIntOrNull()?.takeIf { it in 0..1440 }?.let(::ReminderDelayMinutes)
    }
}
