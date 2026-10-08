package com.vitahealth.tata.omission.application

import com.vitahealth.tata.shared.common.result.AppResult

/** Follows an FCM topic on this device. Returns false when push is not configured. */
interface PushTopicSubscriptions {
    fun subscribe(topic: String): Boolean
}

/** Who this device follows, so a tapped notification can open the right screen. */
interface PushTargetStore {
    fun rememberCaregiver(olderAdultId: String, caregiverId: String)
    fun rememberOlderAdult(olderAdultId: String, olderAdultName: String)
    fun caregiverFor(olderAdultId: String): String?
    fun olderAdultName(olderAdultId: String): String?
}

/** Sends the FCM registration token of this device to the backend. */
interface DeviceTokenRegistry {
    suspend fun register(token: String): AppResult<Unit>
}

object OmissionFailureCodes {
    const val DEVICE_TOKEN_ENDPOINT_MISSING = "DEVICE_TOKEN_ENDPOINT_MISSING"
    const val INVALID_REFERENCE = "INVALID_FOLLOW_UP_REFERENCE"
}
