package com.vitahealth.tata.app.integration.consentwithdrawal

import com.vitahealth.tata.carelink.application.ports.ConsentWithdrawal
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Coordinates CareLink withdrawal with device unsubscription and Identity session termination. */
class ConsentWithdrawalIntegration(
    private val sessions: SessionStore,
    private val withdrawal: ConsentWithdrawal,
    private val beforeSignOut: suspend () -> Unit,
    private val signOut: suspend () -> AppResult<Unit>,
) : ConsentWithdrawal {
    override suspend fun withdraw(olderAdultId: String): AppResult<Unit> {
        val captured =
            sessions.session()
                ?: return AppResult.Failure("Sesión requerida.", code = "AUTHENTICATION_REQUIRED")
        if (captured.owner?.role != "OLDER_ADULT" || captured.owner.subjectId != olderAdultId)
            return AppResult.Failure("Cuenta no autorizada.", code = "RESOURCE_ACCESS_DENIED")
        val result = withdrawal.withdraw(olderAdultId)
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured)) return changed()
        if (result is AppResult.Failure) return result
        beforeSignOut()
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured)) return changed()
        signOut()
        currentCoroutineContext().ensureActive()
        // Remote logout can fail after withdrawal; Identity still clears only its captured token.
        return if (sessions.session() == null) AppResult.Success(Unit) else changed()
    }

    private fun changed() =
        AppResult.Failure(
            "La sesión cambió. Vuelve a ingresar con tu cuenta.",
            code = "SESSION_CHANGED",
        )
}
