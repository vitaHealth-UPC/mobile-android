package com.vitahealth.tata.carelink.application.queries

import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult

/** Enforces required references before calling the account-scoped repository. */
class ListCaregiverProfilesQueryHandler(private val repository: CaregiverProfilesRepository) {
    suspend operator fun invoke(query: ListCaregiverProfilesQuery): AppResult<List<LinkedAdult>> {
        if (query.caregiverId.isBlank()) return AppResult.Failure("Invalid caregiver", code = "INVALID_CAREGIVER_REFERENCE")
        return repository.list(query.caregiverId)
    }
}
