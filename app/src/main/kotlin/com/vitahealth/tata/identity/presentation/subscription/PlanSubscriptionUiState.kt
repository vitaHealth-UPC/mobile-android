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

/** Outcome of a plan change, shown under the plans. Each value maps to a string resource. */
enum class PlanChangeMessage {
    Updated,
    ErrorPlanUnavailable,
    ErrorAccount,
    ErrorSession,
    ErrorOffline,
    ErrorGeneric,
}

data class PlanSubscriptionUiState(
    val isLoading: Boolean = true,
    val subscription: Subscription? = null,
    val plans: List<Plan> = emptyList(),
    val message: PlanMessage? = null,
    /** The plan the caregiver tapped, not yet confirmed. */
    val selectedPlan: Plan? = null,
    /** The plan waiting for the confirmation dialog. */
    val confirming: Plan? = null,
    val isChanging: Boolean = false,
    val changeMessage: PlanChangeMessage? = null,
) {
    val changeMessageIsError: Boolean
        get() = changeMessage != null && changeMessage != PlanChangeMessage.Updated
}
