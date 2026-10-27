package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory

/** Payload for creating a personal note in the current older-adult account. */
data class CreatePersonalNoteRequest(
    val title: String,
    val text: String,
    val category: PersonalNoteCategory,
)
