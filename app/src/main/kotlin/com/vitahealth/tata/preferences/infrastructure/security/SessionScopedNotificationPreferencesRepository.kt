package com.vitahealth.tata.preferences.infrastructure.security

import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.repositories.NotificationPreferencesRepository
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Binds notification settings and contact recipients to the initiating authenticated session. */
class SessionScopedNotificationPreferencesRepository(
    private val sessions: SessionStore,
    private val remoteFor: (StoredSession) -> NotificationPreferencesRepository,
) : NotificationPreferencesRepository {
    override suspend fun get(userId: String): AppResult<NotificationPreferences> =
        withOwner(userId) { it.get(userId) }

    override suspend fun update(
        userId: String,
        preferences: NotificationPreferences,
    ): AppResult<NotificationPreferences> = withOwner(userId) { it.update(userId, preferences) }

    override suspend fun updatePolicy(
        userId: String,
        policy: NotificationPolicy,
        contacts: List<EmergencyContact>,
    ): AppResult<NotificationPreferences> =
        withOwner(userId) { it.updatePolicy(userId, policy, contacts) }

    private suspend fun withOwner(
        userId: String,
        operation: suspend (NotificationPreferencesRepository) -> AppResult<NotificationPreferences>,
    ): AppResult<NotificationPreferences> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (
            captured == null ||
                owner == null ||
                owner.role == "LINK_SETUP" ||
                !captured.expiresAt.isAfter(Instant.now())
        ) {
            return AppResult.Failure(
                "Inicia sesión para modificar tus preferencias.",
                code = "SESSION_REQUIRED",
            )
        }
        if (owner.subjectId != userId)
            return AppResult.Failure(
                "Las preferencias pertenecen a otra cuenta.",
                code = "PREFERENCES_OWNER_MISMATCH",
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
                "La sesión cambió. Abre nuevamente tus preferencias.",
                code = "SESSION_CHANGED",
            )
        }
        return result
    }
}
