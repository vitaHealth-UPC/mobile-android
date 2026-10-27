package com.vitahealth.tata.treatment.application.queries

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel

/** Validates treatment references before reading the configured regimen. */
class GetTreatmentDetailQueryHandler(private val repository: TreatmentDetailRepository) {
    suspend operator fun invoke(
        query: GetTreatmentDetailQuery
    ): AppResult<TreatmentDetailReadModel> {
        val caregiverId = query.caregiverId.trim()
        val treatmentId = query.treatmentId.trim()

        if (caregiverId.isBlank() || treatmentId.isBlank()) {
            return AppResult.Failure(
                message = "No se pudo identificar el tratamiento.",
                code = "INVALID_TREATMENT_REFERENCE",
            )
        }

        return repository.getTreatmentDetail(caregiverId = caregiverId, treatmentId = treatmentId)
    }
}
