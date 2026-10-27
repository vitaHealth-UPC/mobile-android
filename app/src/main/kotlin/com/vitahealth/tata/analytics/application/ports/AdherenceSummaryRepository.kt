package com.vitahealth.tata.analytics.application.ports

import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Provides adherence summaries or an absent result when evidence is insufficient. */
interface AdherenceSummaryRepository {
    suspend fun getSummary(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?>
}
