package com.vitahealth.tata.monitoring.application.ports

import com.vitahealth.tata.monitoring.application.readmodels.AlertTimeline
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

fun interface AlertTimelineReader {
    suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<AlertTimeline>
}
