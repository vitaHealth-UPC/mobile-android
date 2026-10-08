package com.vitahealth.tata.monitoring.application

import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.shared.common.result.AppResult

interface PersonalNotesRepository {
    suspend fun list(): AppResult<List<PersonalNote>>

    suspend fun create(
        title: String,
        text: String,
        category: PersonalNoteCategory,
    ): AppResult<PersonalNote>
}
