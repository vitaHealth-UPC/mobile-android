package com.vitahealth.tata.identity.application

import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

interface IdentityRepository {
    suspend fun registerCaregiver(name: String, email: String, password: String): AppResult<CaregiverAccount>
    suspend fun verifyEmail(email: String, code: String): AppResult<CaregiverAccount>
}
