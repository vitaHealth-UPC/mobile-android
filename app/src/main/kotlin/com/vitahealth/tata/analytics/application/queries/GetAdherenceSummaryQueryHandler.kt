package com.vitahealth.tata.analytics.application.queries

import com.vitahealth.tata.analytics.application.ports.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Loads adherence history within the period supported by the backend. */
class GetAdherenceSummaryQueryHandler(private val repository: AdherenceSummaryRepository) {
    suspend operator fun invoke(
        query: GetAdherenceSummaryQuery
    ): AppResult<AdherenceSummaryReadModel?> {
        val olderAdultId = query.olderAdultId.trim()
        if (olderAdultId.isBlank()) {
            return AppResult.Failure(
                message = "No se pudo identificar al adulto mayor.",
                code = "INVALID_OLDER_ADULT_REFERENCE",
            )
        }
        if (query.periodDays !in 1..31) {
            return AppResult.Failure(
                message = "El periodo seleccionado no es válido.",
                code = "INVALID_PERIOD",
            )
        }
        return repository.getSummary(olderAdultId, query.periodDays)
    }
}
