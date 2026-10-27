package com.vitahealth.tata.monitoring.infrastructure.security

import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Captures and verifies the caregiver session around one protected follow-up operation. */
internal class CaregiverSessionBoundary(private val sessions: SessionStore) {
    suspend fun <T> run(
        caregiverId: String,
        operation: suspend (StoredSession) -> AppResult<T>,
    ): AppResult<T> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (captured == null || owner == null || !captured.expiresAt.isAfter(Instant.now())) {
            return AppResult.Failure(
                "Inicia sesión para consultar el seguimiento.",
                code = "SESSION_REQUIRED",
            )
        }
        if (owner.role != "CAREGIVER" || owner.subjectId != caregiverId) {
            return AppResult.Failure(
                "La cuenta no corresponde al cuidador solicitado.",
                code = "CARE_RELATIONSHIP_REQUIRED",
            )
        }
        val result = operation(captured)
        currentCoroutineContext().ensureActive()
        val current = sessions.session()
        if (
            current?.accessToken != captured.accessToken ||
                current.owner != owner ||
                !current.expiresAt.isAfter(Instant.now())
        ) {
            return AppResult.Failure(
                "La sesión cambió. Abre nuevamente el seguimiento.",
                code = "SESSION_CHANGED",
            )
        }
        return result
    }
}
