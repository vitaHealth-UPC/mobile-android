package com.vitahealth.tata.identity.application

import com.vitahealth.tata.identity.domain.model.AccountStatus
import com.vitahealth.tata.identity.domain.model.CaregiverAccount
import com.vitahealth.tata.shared.common.result.AppResult

val pendingAccount = CaregiverAccount("account-1", "Diego", "diego@example.com", AccountStatus.PENDING_VERIFICATION)
val activeAccount = pendingAccount.copy(status = AccountStatus.ACTIVE)

/** A backend that always answers with [result] and remembers what it was asked. */
class FakeIdentityRepository(
    var result: AppResult<CaregiverAccount> = AppResult.Success(pendingAccount),
) : IdentityRepository {
    val registrations = mutableListOf<Triple<String, String, String>>()
    val verifications = mutableListOf<Pair<String, String>>()
    val newCodeRequests = mutableListOf<String>()

    override suspend fun registerCaregiver(name: String, email: String, password: String): AppResult<CaregiverAccount> {
        registrations += Triple(name, email, password)
        return result
    }

    override suspend fun verifyEmail(email: String, code: String): AppResult<CaregiverAccount> {
        verifications += email to code
        return result
    }

    override suspend fun requestNewVerification(email: String): AppResult<CaregiverAccount> {
        newCodeRequests += email
        return result
    }
}

fun failure(code: String) = AppResult.Failure(message = code.lowercase(), code = code)
