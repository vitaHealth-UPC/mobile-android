package com.vitahealth.tata.carelink.application

import com.vitahealth.tata.carelink.domain.model.CareLink
import com.vitahealth.tata.carelink.domain.model.OlderAdultProfile
import com.vitahealth.tata.shared.common.result.AppResult

interface CareLinkRepository {
    suspend fun acceptLink(caregiverId: String, code: String): AppResult<CareLink>
    suspend fun getOlderAdult(olderAdultId: String): AppResult<OlderAdultProfile>
}
