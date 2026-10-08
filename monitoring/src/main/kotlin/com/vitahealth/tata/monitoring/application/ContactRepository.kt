package com.vitahealth.tata.monitoring.application

import com.vitahealth.tata.monitoring.domain.model.ContactChannel
import com.vitahealth.tata.shared.common.result.AppResult

interface ContactRepository {
    /** Fails with [AlertFailureCodes.NOT_FOUND] when there is no follow-up or no channel. */
    suspend fun contactChannel(caregiverId: String, olderAdultId: String): AppResult<ContactChannel>
    suspend fun olderAdultFullName(olderAdultId: String): AppResult<String>
}
