package com.vitahealth.tata.carelink.application.ports

import com.vitahealth.tata.shared.common.result.AppResult

/** Updates the profile contact, independently of notification-policy recipients. */
fun interface LinkedPersonEmergencyContactUpdater {
    suspend fun update(
        caregiverId: String,
        olderAdultId: String,
        name: String,
        relationship: String,
        phone: String,
    ): AppResult<Unit>
}
