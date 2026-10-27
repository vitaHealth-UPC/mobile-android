package com.vitahealth.tata.identity.application.queries

import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Loads the subscription after validating its account reference. */
class GetCurrentSubscriptionQueryHandler(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(query: GetCurrentSubscriptionQuery): AppResult<Subscription> {
        val accountId = query.accountId.trim()
        if (accountId.isBlank()) {
            return AppResult.Failure(
                message = "The account could not be identified.",
                code = "INVALID_ACCOUNT_REFERENCE",
            )
        }
        return when (val result = repository.currentSubscription(accountId)) {
            is AppResult.Failure -> result
            is AppResult.Success ->
                if (result.value.accountId == accountId) {
                    result
                } else {
                    AppResult.Failure(
                        message = "The subscription belongs to a different account.",
                        code = "INVALID_SUBSCRIPTION_RESPONSE",
                    )
                }
        }
    }
}
