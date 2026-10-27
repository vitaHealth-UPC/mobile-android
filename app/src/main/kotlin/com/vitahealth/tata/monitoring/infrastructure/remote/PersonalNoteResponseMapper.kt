package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import java.time.Instant

/** Rejects malformed note identities, content, timestamps and unknown category codes. */
internal fun PersonalNoteResponse.toDomain(): PersonalNote {
    require(id > 0 && title.isNotBlank() && text.isNotBlank())
    return PersonalNote(id, title, text, requireNotNull(category), Instant.parse(recordedAt))
}
