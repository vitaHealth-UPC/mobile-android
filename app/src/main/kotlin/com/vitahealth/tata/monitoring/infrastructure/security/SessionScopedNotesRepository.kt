package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.repositories.NotesRepository
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

/** Binds follow-up note reads and creation to the initiating caregiver's credentials. */
class SessionScopedNotesRepository(
    sessions: SessionStore,
    private val remoteFor: (StoredSession) -> NotesRepository,
) : NotesRepository {
    private val boundary = CaregiverSessionBoundary(sessions)

    override suspend fun notes(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<FollowUpNote>> =
        boundary.run(caregiverId) { remoteFor(it).notes(caregiverId, olderAdultId) }

    override suspend fun register(
        caregiverId: String,
        olderAdultId: String,
        text: String,
    ): AppResult<FollowUpNote> =
        boundary.run(caregiverId) { remoteFor(it).register(caregiverId, olderAdultId, text) }
}
