package com.vitahealth.tata.identity.application.queries

import com.vitahealth.tata.identity.domain.model.entities.Plan
import com.vitahealth.tata.identity.domain.repositories.SubscriptionRepository
import com.vitahealth.tata.shared.common.result.AppResult
/** Loads the server-defined plan catalog without assuming a selected account. */
class ListAvailablePlansQueryHandler(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(
        @Suppress("UNUSED_PARAMETER") query: ListAvailablePlansQuery = ListAvailablePlansQuery
    ): AppResult<List<Plan>> = repository.listPlans()
}
