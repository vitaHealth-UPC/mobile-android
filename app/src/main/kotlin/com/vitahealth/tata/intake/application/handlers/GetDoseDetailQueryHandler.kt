package com.vitahealth.tata.intake.application.handlers

import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQuery
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class GetDoseDetailQueryHandler(
    private val repository: DoseDetailRepository,
) {
    suspend operator fun invoke(query: GetDoseDetailQuery): AppResult<DoseDetailReadModel> {
        val intakeId = query.intakeId.trim()
        if (intakeId.isBlank()) {
            return AppResult.Failure(
                message = "No se pudo identificar la toma.",
                code = "INVALID_INTAKE_REFERENCE",
            )
        }
        return repository.getDoseDetail(intakeId)
    }
}
