package com.vitahealth.tata.monitoring.application.queries

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.application.ports.AlertTimelineReader
import com.vitahealth.tata.monitoring.application.readmodels.AlertTimeline
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

class GetAlertTimelineQueryHandler(private val reader: AlertTimelineReader) {
    suspend operator fun invoke(
        caregiverId: String,
        olderAdultId: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<AlertTimeline> {
        if (caregiverId.isBlank() || olderAdultId.isBlank())
            return AppResult.Failure(
                "No se pudo identificar el seguimiento.",
                code = AlertFailureCodes.INVALID_REFERENCE,
            )
        return when (val result = reader.read(caregiverId.trim(), olderAdultId.trim(), day, zone)) {
            is AppResult.Success ->
                AppResult.Success(
                    result.value.copy(alerts = result.value.alerts.sortedWith(alertOrder))
                )
            is AppResult.Failure -> result
        }
    }
}
