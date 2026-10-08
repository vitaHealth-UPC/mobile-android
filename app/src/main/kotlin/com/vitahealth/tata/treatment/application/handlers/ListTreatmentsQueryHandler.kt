package com.vitahealth.tata.treatment.application.handlers
import com.vitahealth.tata.treatment.application.TreatmentCatalogRepository
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQuery
import com.vitahealth.tata.shared.common.result.AppResult
class ListTreatmentsQueryHandler(private val repository: TreatmentCatalogRepository) {
    suspend operator fun invoke(query: ListTreatmentsQuery) = if(query.caregiverId.isBlank() || query.olderAdultId.isBlank()) AppResult.Failure("Invalid treatment owner",code="INVALID_REFERENCE") else repository.listTreatments(query.caregiverId,query.olderAdultId)
}
