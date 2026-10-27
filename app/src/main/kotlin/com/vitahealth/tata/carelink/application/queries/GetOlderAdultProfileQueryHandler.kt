package com.vitahealth.tata.carelink.application.queries

import com.vitahealth.tata.carelink.domain.repositories.CareLinkRepository
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.shared.common.result.AppResult

/** Retrieves the profile needed to present the linked adult. */
class GetOlderAdultProfileQueryHandler(private val repository: CareLinkRepository) {
    suspend operator fun invoke(query: GetOlderAdultProfileQuery): AppResult<OlderAdultProfile> {
        if (query.olderAdultId.isBlank()) {
            return AppResult.Failure("Invalid reference", code = "INVALID_ADULT_REFERENCE")
        }
        return repository.getOlderAdult(query.olderAdultId)
    }
}
