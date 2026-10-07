package com.vitahealth.tata.identity.presentation.subscription

import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.identity.domain.model.Subscription

/** Why the plans could not be shown. Each value maps to a string resource. */
enum class PlanMessage {
    ErrorOffline,
    ErrorAccount,
    ErrorSession,
    ErrorGeneric,
}

data class PlanSubscriptionUiState(
    val isLoading: Boolean = true,
    val subscription: Subscription? = null,
    val plans: List<Plan> = emptyList(),
    val message: PlanMessage? = null,
)
