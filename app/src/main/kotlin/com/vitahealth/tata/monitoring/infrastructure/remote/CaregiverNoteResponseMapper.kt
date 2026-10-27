package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import java.time.DateTimeException
import java.time.OffsetDateTime

/** Validates the persisted note identity, author, text and recording instant. */
fun CaregiverNoteResponse.toDomain(): FollowUpNote =
    FollowUpNote(
        id = requireNotNull(id) { "note id is missing" },
        text = requireNotNull(text) { "note text is missing" },
        recordedAt =
            try {
                OffsetDateTime.parse(requireNotNull(recordedAt) { "note date is missing" })
                    .toInstant()
            } catch (exception: DateTimeException) {
                throw IllegalArgumentException("invalid note date: $recordedAt", exception)
            },
        familiarId =
            requireNotNull(familiarId?.takeIf { it.isNotBlank() }) { "note author is missing" },
    )
