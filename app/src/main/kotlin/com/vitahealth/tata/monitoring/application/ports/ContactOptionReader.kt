package com.vitahealth.tata.monitoring.application.ports

import com.vitahealth.tata.monitoring.application.queries.GetContactOptionQuery
import com.vitahealth.tata.monitoring.application.readmodels.ContactOption
import com.vitahealth.tata.shared.common.result.AppResult

/** Resolves the contact channel and display name as one follow-up operation. */
fun interface ContactOptionReader {
    suspend operator fun invoke(query: GetContactOptionQuery): AppResult<ContactOption>
}
