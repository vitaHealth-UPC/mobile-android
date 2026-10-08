package com.vitahealth.tata.analytics.application

import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult

interface AdherenceSummaryRepository {
    suspend fun getSummary(olderAdultId: String, periodDays: Int): AppResult<AdherenceSummaryReadModel?>
}
