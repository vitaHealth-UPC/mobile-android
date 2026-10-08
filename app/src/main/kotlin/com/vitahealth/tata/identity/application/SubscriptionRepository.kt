package com.vitahealth.tata.identity.application

import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.shared.common.result.AppResult

interface SubscriptionRepository {
    suspend fun listPlans(): AppResult<List<Plan>>

    suspend fun currentSubscription(accountId: String): AppResult<Subscription>

    /** Activates or changes the plan; the backend keeps the account, its history and its links. */
    suspend fun changeSubscription(accountId: String, planCode: String): AppResult<Subscription>
}
