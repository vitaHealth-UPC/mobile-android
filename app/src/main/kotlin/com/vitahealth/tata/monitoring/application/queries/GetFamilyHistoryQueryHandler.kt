package com.vitahealth.tata.monitoring.application.queries

import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.shared.common.result.AppResult

/** Validates the request and delegates to its authorized repository port. */
class GetFamilyHistoryQueryHandler(private val repository: FamilyMonitoringRepository) {
    suspend operator fun invoke(query: GetFamilyHistoryQuery): AppResult<List<IntakeHistoryEntry>> {
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank()) {
            return AppResult.Failure("Invalid caregiver/adult", code = "INVALID_REFERENCE")
        }
        return repository.history(query.caregiverId.trim(), query.olderAdultId.trim())
    }
}
