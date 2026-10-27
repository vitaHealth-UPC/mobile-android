package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.repositories.AlertsRepository
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

/** Binds alert reads and explicit status transitions to the initiating caregiver. */
class SessionScopedAlertsRepository(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> AlertsRepository,
) : AlertsRepository {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun openAlerts(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<CaregiverAlert>> =
        boundary.run(caregiverId) { remoteFor(it).openAlerts(caregiverId, olderAdultId) }

    override suspend fun alert(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
    ): AppResult<CaregiverAlert> =
        boundary.run(caregiverId) { remoteFor(it).alert(caregiverId, olderAdultId, alertId) }

    override suspend fun updateStatus(
        caregiverId: String,
        olderAdultId: String,
        alertId: Long,
        status: AlertStatus,
    ): AppResult<CaregiverAlert> =
        boundary.run(caregiverId) {
            remoteFor(it).updateStatus(caregiverId, olderAdultId, alertId, status)
        }
}
