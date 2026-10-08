package com.vitahealth.tata.carelink.application.handlers

import com.vitahealth.tata.carelink.application.CareLinkRepository
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQuery

class GetOlderAdultProfileQueryHandler(
    private val repository: CareLinkRepository,
) {
    suspend operator fun invoke(query: GetOlderAdultProfileQuery) =
        repository.getOlderAdult(query.olderAdultId)
}
