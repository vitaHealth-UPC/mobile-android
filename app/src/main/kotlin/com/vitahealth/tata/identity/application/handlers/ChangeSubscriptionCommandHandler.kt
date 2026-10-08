package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.SubscriptionRepository
import com.vitahealth.tata.identity.application.commands.ChangeSubscriptionCommand
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.shared.common.result.AppResult

class ChangeSubscriptionCommandHandler(
    private val repository: SubscriptionRepository,
) {
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
        return repository.changeSubscription(accountId, planCode)
    }
}
