package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.FamilyMonitoringRepository
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.IntakeHistoryEntry
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.LocalDate
import java.time.ZoneId

/** Keeps every request of a monitoring operation on its initiating caregiver's credentials. */
class SessionScopedFamilyMonitoringRepository(
    private val sessions: SessionStore,
    private val remoteFor: (StoredSession) -> FamilyMonitoringRepository,
) : FamilyMonitoringRepository {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun summary(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        day: LocalDate,
        zone: ZoneId,
    ): AppResult<FamilySummary> =
        boundary.run(caregiverId) {
            remoteFor(it).summary(caregiverId, olderAdultId, name, day, zone)
        }

    override suspend fun history(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<IntakeHistoryEntry>> =
        boundary.run(caregiverId) { remoteFor(it).history(caregiverId, olderAdultId) }
}
