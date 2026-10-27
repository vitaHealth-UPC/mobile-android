package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.AlertHistoryReader
import com.vitahealth.tata.monitoring.application.readmodels.RecordedAlertEvent
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

class SessionScopedAlertHistoryReader(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> AlertHistoryReader,
) : AlertHistoryReader {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
    ): AppResult<List<RecordedAlertEvent>> =
        boundary.run(caregiverId) { remoteFor(it).read(caregiverId, olderAdultId, alertId) }
}
