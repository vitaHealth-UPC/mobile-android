package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.AlertTimelineReader
import com.vitahealth.tata.monitoring.application.readmodels.AlertTimeline
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

class SessionScopedAlertTimelineReader(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> AlertTimelineReader,
) : AlertTimelineReader {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<AlertTimeline> =
        boundary.run(caregiverId) { remoteFor(it).read(caregiverId, olderAdultId, day, zone) }
}
