package com.vitahealth.tata.monitoring.application

import com.vitahealth.tata.monitoring.domain.model.FamilySummary
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

interface FamilyMonitoringRepository {
    suspend fun summary(caregiverId: String, olderAdultId: String, name: String, day: LocalDate, zone: ZoneId): AppResult<FamilySummary>
    suspend fun contact(caregiverId: String, olderAdultId: String): AppResult<String>
    suspend fun history(caregiverId: String, olderAdultId: String): AppResult<List<String>>
    suspend fun notes(caregiverId: String, olderAdultId: String): AppResult<List<String>>
}
