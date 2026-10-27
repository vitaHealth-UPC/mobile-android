package com.vitahealth.tata.analytics.application.ports

import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads definitive outcomes for one regimen without falling back to adult-wide metrics. */
interface TreatmentAdherenceRepository {
    suspend fun getTreatmentSummary(
        olderAdultId: String,
        treatmentId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?>
}
