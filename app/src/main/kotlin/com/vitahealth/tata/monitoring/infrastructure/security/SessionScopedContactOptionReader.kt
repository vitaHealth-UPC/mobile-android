package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.application.ports.ContactOptionReader
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQueryHandler
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.monitoring.domain.repositories.ContactRepository
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * Keeps both contact lookups on captured credentials and discards results after a session change.
 */
class SessionScopedContactOptionReader(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> ContactRepository,
) : ContactOptionReader {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun invoke(query: GetContactOptionQuery): AppResult<ContactOption> =
        boundary.run(query.caregiverId.trim()) { captured ->
            GetContactOptionQueryHandler(remoteFor(captured))(query)
        }
}
