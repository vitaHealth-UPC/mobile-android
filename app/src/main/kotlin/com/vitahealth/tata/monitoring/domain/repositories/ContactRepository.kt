package com.vitahealth.tata.monitoring.domain.repositories

import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.shared.common.result.AppResult

/** Provides the contact channel and identity available to authorized follow-up. */
interface ContactRepository {
    /** Fails with `ALERT_NOT_FOUND` when there is no follow-up or no channel. */
    suspend fun contactChannel(caregiverId: String, olderAdultId: String): AppResult<ContactChannel>

    suspend fun olderAdultFullName(olderAdultId: String): AppResult<String>
}
