package com.vitahealth.tata.monitoring.application.queries

import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.shared.common.result.AppResult

/** Validates the request and delegates to its authorized repository port. */
class GetFamilySummaryQueryHandler(private val repository: FamilyMonitoringRepository) {
    suspend operator fun invoke(query: GetFamilySummaryQuery): AppResult<FamilySummary> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) {
            return AppResult.Failure("Invalid caregiver/adult", code = "INVALID_REFERENCE")
        }
        return repository.summary(
            query.caregiverId.trim(),
            query.olderAdultId.trim(),
            query.olderAdultName,
            query.day,
            query.zone,
        )
    }
}
