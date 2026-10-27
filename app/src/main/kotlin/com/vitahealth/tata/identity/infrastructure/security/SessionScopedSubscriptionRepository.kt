package com.vitahealth.tata.identity.infrastructure.security

import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.shared.application.session.SessionStore
import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Binds a subscription screen to its caregiver and captures credentials for every operation. */
class SessionScopedSubscriptionRepository(
    private val accountId: String,
    private val sessions: SessionStore,
    private val remoteFor: (StoredSession) -> SubscriptionRepository,
    private val now: () -> Instant = Instant::now,
) : SubscriptionRepository {
    override suspend fun listPlans(): AppResult<List<Plan>> = run(accountId) { it.listPlans() }

    override suspend fun currentSubscription(accountId: String): AppResult<Subscription> =
        run(accountId) { it.currentSubscription(accountId) }

    override suspend fun changeSubscription(
        accountId: String,
        planCode: String,
    ): AppResult<Subscription> = run(accountId) { it.changeSubscription(accountId, planCode) }

    private suspend fun <T> run(
        requestedAccount: String,
        operation: suspend (SubscriptionRepository) -> AppResult<T>,
    ): AppResult<T> {
        val captured = sessions.session()
        val owner = captured?.owner
        if (captured == null || owner == null || !captured.expiresAt.isAfter(now())) {
            return AppResult.Failure("", code = "UNAUTHENTICATED")
        }
        if (
            owner.role != "CAREGIVER" ||
                owner.subjectId != accountId ||
                requestedAccount != accountId
        ) {
            return AppResult.Failure("", code = "ACCOUNT_ACCESS_DENIED")
        }
        currentCoroutineContext().ensureActive()
        val result = operation(remoteFor(captured))
        currentCoroutineContext().ensureActive()
        if (!sessions.isCurrent(captured) || !captured.expiresAt.isAfter(now())) {
            return AppResult.Failure("", code = "SESSION_CHANGED")
        }
        return result
    }
}
