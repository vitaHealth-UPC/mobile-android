package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.MonitoredPersonReader
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

class SessionScopedMonitoredPersonReader(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> MonitoredPersonReader,
) : MonitoredPersonReader {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<MonitoredPerson> =
        boundary.run(caregiverId) { remoteFor(it).read(caregiverId, olderAdultId) }
}
