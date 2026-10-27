package com.vitahealth.tata.identity.domain.repositories

import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.shared.common.result.AppResult
/** Loads the plan catalog and changes the server-owned account subscription. */
interface SubscriptionRepository {
    suspend fun listPlans(): AppResult<List<Plan>>

    suspend fun currentSubscription(accountId: String): AppResult<Subscription>

    /** Activates or changes the plan; the backend keeps the account, its history and its links. */
    suspend fun changeSubscription(accountId: String, planCode: String): AppResult<Subscription>
}
