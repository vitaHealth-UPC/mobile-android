package com.vitahealth.tata.monitoring.application.ports

import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

/** Reads protected family summaries and intake history. */
interface FamilyMonitoringRepository {
    suspend fun summary(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<FamilySummary>

    suspend fun history(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<IntakeHistoryEntry>>
}
