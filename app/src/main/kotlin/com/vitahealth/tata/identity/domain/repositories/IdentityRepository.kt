package com.vitahealth.tata.identity.domain.repositories

import com.vitahealth.tata.identity.domain.model.entities.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult
/** Account registration and verification capability independent of HTTP and device storage. */
interface IdentityRepository {
    /** Registers an account that follows the backend email-verification lifecycle. */
    suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String,
    ): AppResult<CaregiverAccount>

    /** Verifies the current email challenge and returns the updated account. */
    suspend fun verifyEmail(email: String, code: String): AppResult<CaregiverAccount>

    /** Requests another challenge without assuming that verification already succeeded. */
    suspend fun requestNewVerification(email: String): AppResult<CaregiverAccount>
}
