package com.vitahealth.tata.omission.application.ports

import com.vitahealth.tata.shared.common.result.AppResult

/** Reads the SDK's current token; null means this build has no push configuration. */
fun interface DevicePushTokenSource {
    suspend fun currentToken(): AppResult<String?>
}
