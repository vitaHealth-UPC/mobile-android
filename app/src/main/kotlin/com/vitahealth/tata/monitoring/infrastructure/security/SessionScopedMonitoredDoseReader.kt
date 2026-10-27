package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.MonitoredDoseReader
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

class SessionScopedMonitoredDoseReader(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> MonitoredDoseReader,
) : MonitoredDoseReader {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun read(
        caregiverId: String,
        olderAdultId: String,
        intakeId: String,
    ): AppResult<MonitoredDose> =
        boundary.run(caregiverId) { remoteFor(it).read(caregiverId, olderAdultId, intakeId) }
}
