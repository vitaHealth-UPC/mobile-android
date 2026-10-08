package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.SubscriptionRepository
import com.vitahealth.tata.identity.application.queries.ListAvailablePlansQuery
import com.vitahealth.tata.identity.domain.model.Plan
import com.vitahealth.tata.shared.common.result.AppResult

class ListAvailablePlansQueryHandler(
    private val repository: SubscriptionRepository,
) {
    suspend operator fun invoke(
        @Suppress("UNUSED_PARAMETER") query: ListAvailablePlansQuery = ListAvailablePlansQuery,
    ): AppResult<List<Plan>> = repository.listPlans()
}
