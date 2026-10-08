package com.vitahealth.tata.analytics.application

import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.shared.common.result.AppResult

interface AdherenceRecommendationsRepository {
    suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?>
}
