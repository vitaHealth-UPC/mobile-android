package com.vitahealth.tata.analytics.infrastructure.security

import com.vitahealth.tata.analytics.application.ports.AdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.application.ports.AdherenceSummaryRepository
import com.vitahealth.tata.analytics.application.ports.TreatmentAdherenceRepository
import com.vitahealth.tata.analytics.application.readmodels.AdherenceRecommendationsReadModel
import com.vitahealth.tata.analytics.application.readmodels.AdherenceSummaryReadModel
import com.vitahealth.tata.analytics.infrastructure.remote.AnalyticsApiService
import com.vitahealth.tata.analytics.infrastructure.remote.RemoteAdherenceRecommendationsRepository
import com.vitahealth.tata.analytics.infrastructure.remote.RemoteAdherenceSummaryRepository
import com.vitahealth.tata.shared.application.session.SessionOwner
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Keeps analytics screens tied to their account and each query tied to one credential snapshot. */
class SessionScopedAnalyticsRepositories(
    private val sessions: SessionStore,
    private val screenOwner: SessionOwner?,
    private val apiFor: (StoredSession) -> AnalyticsApiService,
    private val now: () -> Instant = Instant::now,
) : AdherenceSummaryRepository, AdherenceRecommendationsRepository, TreatmentAdherenceRepository {
    override suspend fun getTreatmentSummary(
        olderAdultId: String,
        treatmentId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?> =
        scoped(olderAdultId) { captured ->
            RemoteAdherenceSummaryRepository(apiFor(captured))
                .getTreatmentSummary(olderAdultId, treatmentId, periodDays)
        }

    override suspend fun getSummary(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceSummaryReadModel?> =
        scoped(olderAdultId) { captured ->
            RemoteAdherenceSummaryRepository(apiFor(captured)).getSummary(olderAdultId, periodDays)
        }

    override suspend fun getRecommendations(
        olderAdultId: String,
        periodDays: Int,
    ): AppResult<AdherenceRecommendationsReadModel?> =
        scoped(olderAdultId) { captured ->
            RemoteAdherenceRecommendationsRepository(apiFor(captured))
                .getRecommendations(olderAdultId, periodDays)
        }

    private suspend fun <T> scoped(
        olderAdultId: String,
        operation: suspend (StoredSession) -> AppResult<T>,
    ): AppResult<T> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (
            captured == null ||
                owner == null ||
                screenOwner == null ||
                !captured.expiresAt.isAfter(now()) ||
                owner.role !in setOf("CAREGIVER", "OLDER_ADULT")
        ) {
            return AppResult.Failure(
                "Inicia sesión para consultar la adherencia.",
                code = "SESSION_REQUIRED",
            )
        }
        if (owner != screenOwner) return sessionChanged()
        if (owner.role == "OLDER_ADULT" && owner.subjectId != olderAdultId) {
            return AppResult.Failure(
                "No tienes acceso a la adherencia de este adulto.",
                code = "ACCESS_DENIED",
            )
        }
        currentCoroutineContext().ensureActive()
        val result = operation(captured)
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured) || !captured.expiresAt.isAfter(now()))
            return sessionChanged()
        return result
    }

    private fun sessionChanged() =
        AppResult.Failure(
            "La sesión cambió. Vuelve a abrir la adherencia.",
            code = "SESSION_CHANGED",
        )
}
