package com.vitahealth.tata.omission.infrastructure.push

import com.vitahealth.tata.omission.application.DeviceTokenRegistry
import com.vitahealth.tata.omission.application.OmissionFailureCodes
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * PENDING: the deployed API (`/v3/api-docs`) has no endpoint to register an FCM device token, so the token
 * is not sent anywhere. Delivery relies on the FCM topics named after the backend recipients. Replace this
 * class with a Retrofit implementation when the backend publishes the endpoint.
 */
class PendingDeviceTokenRegistry : DeviceTokenRegistry {
    override suspend fun register(token: String): AppResult<Unit> = AppResult.Failure(
        message = "El backend todavía no recibe tokens de dispositivo.",
        code = OmissionFailureCodes.DEVICE_TOKEN_ENDPOINT_MISSING,
    )
}
