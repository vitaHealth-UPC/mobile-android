package com.vitahealth.tata.identity.infrastructure.remote

import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.identity.domain.model.PlanCapability
import com.vitahealth.tata.identity.domain.model.Subscription
import com.vitahealth.tata.identity.domain.model.SubscriptionStatus
import java.time.Instant

/** Null when the plan lacks a field the app needs. Capabilities this app does not know are skipped. */
internal fun PlanDto.toDomain(): Plan? {
    val code = code?.takeIf { it.isNotBlank() } ?: return null
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    val price = monthlyPrice ?: return null
    val currency = currency?.takeIf { it.isNotBlank() } ?: return null
    return Plan(
        code = code,
        name = name,
        monthlyPrice = price,
        currency = currency,
        capabilities = capabilities.orEmpty()
            .mapNotNull { runCatching { PlanCapability.valueOf(it) }.getOrNull() }
            .toSet(),
    )
}

internal fun SubscriptionDto.toDomain(): Subscription? {
    val accountId = accountId?.takeIf { it.isNotBlank() } ?: return null
    val plan = plan?.toDomain() ?: return null
    val status = runCatching { SubscriptionStatus.valueOf(status.orEmpty()) }.getOrNull() ?: return null
    return Subscription(
        accountId = accountId,
        plan = plan,
        status = status,
        renewsAt = renewsAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )
}
