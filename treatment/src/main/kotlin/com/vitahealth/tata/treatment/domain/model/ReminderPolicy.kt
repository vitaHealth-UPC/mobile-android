package com.vitahealth.tata.treatment.domain.model

@JvmInline
value class ReminderDelayMinutes private constructor(val value: Int) {
    companion object {
        fun parse(raw: String): ReminderDelayMinutes? =
            raw.trim()
                .toIntOrNull()
                ?.takeIf { it in 0..1440 }
                ?.let(::ReminderDelayMinutes)
    }
}

data class ReminderPolicy(
    val followUpDelayMinutes: ReminderDelayMinutes,
)
