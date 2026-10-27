package com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel

import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory

/** Personal note list, category filter and composer operation state. */
data class PersonalNotesUiState(
    val notes: List<PersonalNote> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val saveError: Boolean = false,
    val composing: Boolean = false,
    val category: PersonalNoteCategory? = null,
) {
    val requiresSignIn: Boolean
        get() =
            error in
                setOf(
                    "SESSION_REQUIRED",
                    "OLDER_ADULT_SESSION_REQUIRED",
                    "SESSION_CHANGED",
                    "ACCESS_DENIED",
                )
}
