package com.vitahealth.tata.preferences.infrastructure.security

import com.vitahealth.tata.preferences.application.ports.AccessibilityLocalStore
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRemote
import com.vitahealth.tata.preferences.application.ports.UserPreferencesRepository
import com.vitahealth.tata.preferences.application.readmodels.PreferenceUpdate
import com.vitahealth.tata.preferences.domain.model.entities.UserPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.infrastructure.sync.OfflineFirstUserPreferencesRepository
import com.vitahealth.tata.shared.application.session.SessionObserver
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Captures credentials and isolates accessibility persistence for each authenticated owner. */
class SessionScopedUserPreferencesRepository(
    private val sessions: SessionStore,
    private val observer: SessionObserver,
    private val remoteFor: (StoredSession) -> UserPreferencesRemote,
    private val localFor: (SessionOwner) -> AccessibilityLocalStore,
) : UserPreferencesRepository {
    private val ownerOperations = mutableMapOf<SessionOwner, Mutex>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAccessibility(): Flow<AccessibilityPreferences> =
        observer.observeSessions().flatMapLatest { session ->
            val owner = usable(session)?.owner
            if (owner == null) flowOf(AccessibilityPreferences.Defaults)
            else localFor(owner).observe()
        }

    override suspend fun sync(userId: String): AppResult<UserPreferences> =
        withOwner(userId) { it.sync(userId) }

    override suspend fun updateTextSize(
        userId: String,
        textSize: TextSizeLevel,
    ): AppResult<PreferenceUpdate> = withOwner(userId) { it.updateTextSize(userId, textSize) }

    override suspend fun updateHighContrast(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = withOwner(userId) { it.updateHighContrast(userId, enabled) }

    override suspend fun updateDarkTheme(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = withOwner(userId) { it.updateDarkTheme(userId, enabled) }

    override suspend fun updateReducedMotion(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> = withOwner(userId) { it.updateReducedMotion(userId, enabled) }

    override suspend fun updateVoiceConfirmation(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        withOwner(userId) { it.updateVoiceConfirmation(userId, enabled) }

    override suspend fun updateReadingAssistance(
        userId: String,
        enabled: Boolean,
    ): AppResult<PreferenceUpdate> =
        withOwner(userId) { it.updateReadingAssistance(userId, enabled) }

    private fun sessionChanged() =
        AppResult.Failure(
            "La sesión cambió. Abre nuevamente tus preferencias.",
            code = "SESSION_CHANGED",
        )

    private fun usable(session: StoredSession?): StoredSession? =
        session?.takeIf {
            it.owner != null && it.owner.role != "LINK_SETUP" && it.expiresAt.isAfter(Instant.now())
        }

    private suspend fun <T> withOwner(
        userId: String,
        operation: suspend (UserPreferencesRepository) -> AppResult<T>,
    ): AppResult<T> {
        val captured =
            usable(sessions.session())
                ?: return AppResult.Failure(
                    "Inicia sesión para modificar tus preferencias.",
                    code = "SESSION_REQUIRED",
                )
        val owner = requireNotNull(captured.owner)
        if (owner.subjectId != userId)
            return AppResult.Failure(
                "Las preferencias pertenecen a otra cuenta.",
                code = "PREFERENCES_OWNER_MISMATCH",
            )
        val lock = synchronized(ownerOperations) { ownerOperations.getOrPut(owner) { Mutex() } }
        return lock.withLock {
            val before = usable(sessions.session())
            if (before?.accessToken != captured.accessToken || before.owner != owner)
                return@withLock sessionChanged()
            val repository =
                OfflineFirstUserPreferencesRepository(remoteFor(captured), localFor(owner))
            val result = operation(repository)
            val current = usable(sessions.session())
            if (current?.accessToken != captured.accessToken || current.owner != owner)
                sessionChanged()
            else result
        }
    }
}
