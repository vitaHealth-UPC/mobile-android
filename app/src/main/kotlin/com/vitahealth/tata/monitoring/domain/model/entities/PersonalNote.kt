package com.vitahealth.tata.monitoring.domain.model.entities

import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import java.time.Instant

/** Personal reminder owned by the PIN user; caregiver interventions use FollowUpNote. */
data class PersonalNote(
    val id: Long,
    val title: String,
    val text: String,
    val category: PersonalNoteCategory,
    val recordedAt: Instant,
)
