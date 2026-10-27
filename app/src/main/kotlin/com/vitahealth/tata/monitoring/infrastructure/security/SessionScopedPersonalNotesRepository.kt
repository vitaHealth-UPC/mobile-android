package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.domain.repositories.PersonalNotesRepository
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Captures the authenticated older adult for personal note reads and creation. */
class SessionScopedPersonalNotesRepository(
    private val sessions: SessionStore,
    private val remoteFor: (StoredSession) -> PersonalNotesRepository,
) : PersonalNotesRepository {
    override suspend fun list(): AppResult<List<PersonalNote>> = withAdult { it.list() }

    override suspend fun create(
        title: String,
        text: String,
        category: PersonalNoteCategory,
    ): AppResult<PersonalNote> = withAdult { it.create(title, text, category) }

    private suspend fun <T> withAdult(
        operation: suspend (PersonalNotesRepository) -> AppResult<T>
    ): AppResult<T> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (captured == null || owner == null || !captured.expiresAt.isAfter(Instant.now())) {
            return AppResult.Failure(
                "Inicia sesión para consultar tus notas.",
                code = "SESSION_REQUIRED",
            )
        }
        if (owner.role != "OLDER_ADULT")
            return AppResult.Failure(
                "Ingresa con la cuenta del adulto mayor.",
                code = "OLDER_ADULT_SESSION_REQUIRED",
            )
        val result = operation(remoteFor(captured))
        currentCoroutineContext().ensureActive()
        val current = sessions.session()
        if (
            current?.accessToken != captured.accessToken ||
                current.owner != owner ||
                !current.expiresAt.isAfter(Instant.now())
        ) {
            return AppResult.Failure(
                "La sesión cambió. Abre nuevamente tus notas.",
                code = "SESSION_CHANGED",
            )
        }
        return result
    }
}
