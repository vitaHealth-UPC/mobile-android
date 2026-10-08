package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.NextDoseRepository
import com.vitahealth.tata.intake.application.queries.GetNextDoseQuery
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class GetNextDoseQueryHandler(
    private val repository: NextDoseRepository,
) {
    suspend operator fun invoke(query: GetNextDoseQuery): AppResult<NextDoseReadModel?> {
        val olderAdultId = query.olderAdultId.trim()
        if (olderAdultId.isBlank()) {
            return AppResult.Failure(
                message = "No se pudo identificar al adulto mayor.",
                code = "INVALID_OLDER_ADULT_REFERENCE",
            )
        }
        return repository.getNextDose(olderAdultId)
    }
}
