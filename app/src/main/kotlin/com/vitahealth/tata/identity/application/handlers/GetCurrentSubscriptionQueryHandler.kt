package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.SubscriptionRepository
import com.vitahealth.tata.identity.application.queries.GetCurrentSubscriptionQuery
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.shared.common.result.AppResult

class GetCurrentSubscriptionQueryHandler(
    private val repository: SubscriptionRepository,
) {
    suspend operator fun invoke(query: GetCurrentSubscriptionQuery): AppResult<Subscription> {
        val accountId = query.accountId.trim()
        if (accountId.isBlank()) {
            return AppResult.Failure(
                message = "The account could not be identified.",
                code = "INVALID_ACCOUNT_REFERENCE",
            )
        }
        return repository.currentSubscription(accountId)
    }
}
