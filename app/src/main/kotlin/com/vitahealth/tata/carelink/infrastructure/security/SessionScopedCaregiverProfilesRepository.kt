package com.vitahealth.tata.carelink.infrastructure.security

import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Binds profile operations to the caregiver opening the screen and one credential snapshot. */
class SessionScopedCaregiverProfilesRepository(
    private val sessions: SessionStore,
    private val screenCaregiverId: String,
    private val remoteFor: (StoredSession) -> CaregiverProfilesRepository,
    private val now: () -> Instant = Instant::now,
) : CaregiverProfilesRepository {
    override suspend fun list(caregiverId: String): AppResult<List<LinkedAdult>> =
        scoped(caregiverId) { it.list(caregiverId) }

    override suspend fun register(
        caregiverId: String,
        profile: NewAdultProfile,
    ): AppResult<LinkedAdult> = scoped(caregiverId) { it.register(caregiverId, profile) }

    override suspend fun linkingCode(
        caregiverId: String,
        adult: LinkedAdult,
    ): AppResult<ProfileLinkingCode> = scoped(caregiverId) { it.linkingCode(caregiverId, adult) }

    private suspend fun <T> scoped(
        caregiverId: String,
        call: suspend (CaregiverProfilesRepository) -> AppResult<T>,
    ): AppResult<T> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (captured == null || owner == null || !captured.expiresAt.isAfter(now())) {
            return AppResult.Failure(
                "Inicia sesión para consultar los perfiles.",
                code = "AUTHENTICATION_REQUIRED",
            )
        }
        if (
            owner.role != "CAREGIVER" ||
                owner.subjectId != screenCaregiverId ||
                caregiverId != screenCaregiverId
        ) {
            return AppResult.Failure(
                "La cuenta no corresponde al cuidador solicitado.",
                code = "RESOURCE_ACCESS_DENIED",
            )
        }
        currentCoroutineContext().ensureActive()
        val result = call(remoteFor(captured))
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured) || !captured.expiresAt.isAfter(now())) {
            return AppResult.Failure(
                "La sesión cambió. Abre nuevamente los perfiles.",
                code = "SESSION_CHANGED",
            )
        }
        return result
    }
}
