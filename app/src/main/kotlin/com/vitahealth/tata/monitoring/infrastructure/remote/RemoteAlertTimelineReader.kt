package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.ports.AlertTimelineReader
import com.vitahealth.tata.monitoring.application.readmodels.AlertTimeline
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

/** Reads one protected timeline; a failed source never becomes a misleading empty section. */
class RemoteAlertTimelineReader(private val api: FamilyMonitoringApiService) : AlertTimelineReader {
    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<AlertTimeline> = guardedFollowUp {
        val status = api.status(olderAdultId, caregiverId)
        followUpHttpFailure(status)?.let {
            return@guardedFollowUp it
        }
        val alerts = requireNotNull(status.body()).openAlerts.orEmpty().map { it.toDomain() }
        require(alerts.map { it.id }.distinct().size == alerts.size)
        val agenda =
            api.agenda(
                olderAdultId,
                day.atStartOfDay(zone).toInstant().toString(),
                day.plusDays(1).atStartOfDay(zone).toInstant().toString(),
            )
        followUpHttpFailure(agenda)?.let {
            return@guardedFollowUp it
        }
        val doses = requireNotNull(agenda.body()).map { it.toMonitoredDose() }
        require(doses.map { it.id }.distinct().size == doses.size)
        require(doses.all { it.scheduledAt.atZone(zone).toLocalDate() == day })
        val profile = api.profile(olderAdultId)
        followUpHttpFailure(profile)?.let {
            return@guardedFollowUp it
        }
        val person = requireNotNull(profile.body())
        require(person.id == olderAdultId && !person.fullName.isNullOrBlank())
        AppResult.Success(
            AlertTimeline(
                alerts,
                doses.sortedBy { it.scheduledAt },
                requireNotNull(person.fullName),
                day,
            )
        )
    }
}
