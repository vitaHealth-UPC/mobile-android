package com.vitahealth.tata.monitoring.application.commands

import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory

/** One authenticated adult's personal note creation. */
data class CreatePersonalNoteCommand(
    val title: String,
    val text: String,
    val category: PersonalNoteCategory,
)
