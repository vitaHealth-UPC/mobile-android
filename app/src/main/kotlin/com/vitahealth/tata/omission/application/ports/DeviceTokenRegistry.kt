package com.vitahealth.tata.omission.application.ports

import com.vitahealth.tata.shared.application.session.StoredSession
import com.vitahealth.tata.shared.common.result.AppResult

/** Sends the FCM registration token of this device to the backend. */
interface DeviceTokenRegistry {
    suspend fun register(token: String, expectedSession: StoredSession? = null): AppResult<Unit>

    suspend fun unregister(expectedSession: StoredSession? = null): AppResult<Unit>
}
