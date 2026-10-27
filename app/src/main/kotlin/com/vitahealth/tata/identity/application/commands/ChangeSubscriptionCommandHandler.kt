package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.domain.model.entities.Subscription
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Validates account and plan references before requesting a subscription change. */
class ChangeSubscriptionCommandHandler(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(command: ChangeSubscriptionCommand): AppResult<Subscription> {
        val accountId = command.accountId.trim()
        val planCode = command.planCode.trim()

        if (accountId.isBlank()) {
            return AppResult.Failure(
                message = "The account could not be identified.",
                code = "INVALID_ACCOUNT_REFERENCE",
            )
        }
        if (planCode.isBlank()) {
            return AppResult.Failure(
                message = "Choose a plan first.",
                code = "INVALID_PLAN_REFERENCE",
            )
        }
        return when (val result = repository.changeSubscription(accountId, planCode)) {
            is AppResult.Failure -> result
            is AppResult.Success ->
                if (result.value.accountId == accountId && result.value.plan.code == planCode) {
                    result
                } else {
                    AppResult.Failure(
                        message = "The subscription change receipt did not match the request.",
                        code = "INVALID_SUBSCRIPTION_RESPONSE",
                    )
                }
        }
    }
}
