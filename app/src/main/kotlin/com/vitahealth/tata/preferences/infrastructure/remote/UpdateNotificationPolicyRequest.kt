package com.vitahealth.tata.preferences.infrastructure.remote

/** Policy and recipient replacement request. */
data class UpdateNotificationPolicyRequest(
    val policy: NotificationPolicyDto,
    val contacts: List<EmergencyContactDto>,
)
