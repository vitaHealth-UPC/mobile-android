package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory

/** Transport representation of an older adult's personal note. */
data class PersonalNoteResponse(
    val id: Long,
    val title: String,
    val text: String,
    val category: PersonalNoteCategory?,
    val recordedAt: String,
)
