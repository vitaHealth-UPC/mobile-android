package com.vitahealth.tata.monitoring.domain.model

import java.time.Instant

/** Personal reminder owned by the PIN user; caregiver interventions use FollowUpNote. */
data class PersonalNote(
    val id: Long,
    val title: String,
    val text: String,
    val category: PersonalNoteCategory,
    val recordedAt: Instant,
)

enum class PersonalNoteCategory {
    MEDICATION,
    ROUTINE,
}
