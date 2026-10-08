package com.vitahealth.tata.analytics.application.handlers

import com.vitahealth.tata.analytics.application.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.queries.GetAdherenceRecommendationsQuery
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.shared.common.result.AppResult

class GetAdherenceRecommendationsQueryHandler(
    private val repository: AdherenceRecommendationsRepository,
) {
    suspend operator fun invoke(
        query: GetAdherenceRecommendationsQuery,
    ): AppResult<AdherenceRecommendationsReadModel?> {
        val olderAdultId = query.olderAdultId.trim()
        if (olderAdultId.isBlank()) {
            return AppResult.Failure(
                message = "No se pudo identificar al adulto mayor.",
                code = "INVALID_OLDER_ADULT_REFERENCE",
            )
        }
        if (query.periodDays <= 0) {
            return AppResult.Failure(
                message = "El periodo seleccionado no es válido.",
                code = "INVALID_PERIOD",
            )
        }
        return repository.getRecommendations(olderAdultId, query.periodDays)
    }
}
