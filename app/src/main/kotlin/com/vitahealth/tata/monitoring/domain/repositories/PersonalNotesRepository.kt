package com.vitahealth.tata.monitoring.domain.repositories

import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads and creates notes owned by the authenticated older adult. */
interface PersonalNotesRepository {
    suspend fun list(): AppResult<List<PersonalNote>>

    suspend fun create(
        title: String,
        text: String,
        category: PersonalNoteCategory,
    ): AppResult<PersonalNote>
}
