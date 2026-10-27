package com.vitahealth.tata.analytics.application.ports

import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Provides recommendations derived from the adult's adherence evidence. */
interface AdherenceRecommendationsRepository {
    suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?>
}
