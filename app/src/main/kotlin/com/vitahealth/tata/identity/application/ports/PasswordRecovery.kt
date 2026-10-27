package com.vitahealth.tata.identity.application.ports

import com.vitahealth.tata.shared.common.result.AppResult

/** Anonymous recovery does not install a session or disclose whether an email is registered. */
interface PasswordRecovery {
    suspend fun request(email: String): AppResult<Unit>

    suspend fun complete(email: String, code: String, newPassword: String): AppResult<Unit>
}
