package com.vitahealth.tata.carelink.domain.repositories

import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile
import com.vitahealth.tata.shared.common.result.AppResult

/** Defines linking and consent operations with their associated adult profile. */
interface CareLinkRepository {
    suspend fun acceptLink(caregiverId: String, code: String): AppResult<CareLink>

    suspend fun registerConsent(careLinkId: String, accepted: Boolean): AppResult<CareLink>

    suspend fun getOlderAdult(olderAdultId: String): AppResult<OlderAdultProfile>
}
