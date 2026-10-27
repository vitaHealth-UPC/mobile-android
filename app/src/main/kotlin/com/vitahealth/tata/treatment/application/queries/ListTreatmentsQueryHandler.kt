package com.vitahealth.tata.treatment.application.queries

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.TreatmentCatalogRepository

/** Validates the selected caregiver and adult before reading their treatment catalog. */
class ListTreatmentsQueryHandler(private val repository: TreatmentCatalogRepository) {
    suspend operator fun invoke(query: ListTreatmentsQuery) =
        if (query.caregiverId.isBlank() || query.olderAdultId.isBlank())
            AppResult.Failure("Invalid treatment owner", code = "INVALID_REFERENCE")
        else repository.listTreatments(query.caregiverId, query.olderAdultId)
}
