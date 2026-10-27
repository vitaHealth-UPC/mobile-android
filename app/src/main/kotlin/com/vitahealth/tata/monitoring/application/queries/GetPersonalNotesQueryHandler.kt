package com.vitahealth.tata.monitoring.application.queries

import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.shared.common.result.AppResult

/** Validates the request and delegates to its authorized repository port. */
class GetPersonalNotesQueryHandler(private val repository: PersonalNotesRepository) {
    suspend operator fun invoke(query: GetPersonalNotesQuery): AppResult<List<PersonalNote>> {
        return repository.list()
    }
}
