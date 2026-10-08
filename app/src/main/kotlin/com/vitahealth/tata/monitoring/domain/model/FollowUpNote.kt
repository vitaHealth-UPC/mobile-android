package com.vitahealth.tata.monitoring.domain.model

import java.time.Instant

/** Follow-up note a caregiver writes about an older adult. Notes belong to the follow-up, not to one alert. */
data class FollowUpNote(
    val id: Long,
    val text: String,
    val recordedAt: Instant,
    val familiarId: String,
) {
    init {
        require(id > 0) { "note id must be positive" }
    }

    companion object {
        /** `maxLength` of `CreateCaregiverNoteResource.text`. */
        const val MAX_LENGTH = 1000

        /** The backend accepts an empty text (`minLength` 0); the app does not, because a blank note says nothing. */
        fun problemWith(text: String): NoteTextProblem? = when {
            text.isBlank() -> NoteTextProblem.BLANK
            text.trim().length > MAX_LENGTH -> NoteTextProblem.TOO_LONG
            else -> null
        }
    }
}

enum class NoteTextProblem { BLANK, TOO_LONG }
