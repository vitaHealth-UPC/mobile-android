package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.ports.AlertHistoryReader
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

class RemoteAlertHistoryReader(private val api: FamilyMonitoringApiService) : AlertHistoryReader {
    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
    ): AppResult<List<RecordedAlertEvent>> = guardedFollowUp {
        val response = api.alertTimeline(olderAdultId, alertId, caregiverId)
        followUpHttpFailure(response)?.let {
            return@guardedFollowUp it
        }
        val events =
            requireNotNull(response.body()).map {
                RecordedAlertEvent(
                    RecordedAlertEvent.Type.valueOf(requireNotNull(it.type)),
                    try {
                        Instant.parse(requireNotNull(it.occurredAt))
                    } catch (error: java.time.DateTimeException) {
                        throw IllegalArgumentException("Invalid event timestamp", error)
                    },
                )
            }
        require(events.map { it.type }.distinct().size == events.size)
        require(events.any { it.type == RecordedAlertEvent.Type.INTAKE_SCHEDULED })
        require(events.any { it.type == RecordedAlertEvent.Type.ALERT_GENERATED })
        AppResult.Success(events.sortedBy { it.occurredAt })
    }
}
