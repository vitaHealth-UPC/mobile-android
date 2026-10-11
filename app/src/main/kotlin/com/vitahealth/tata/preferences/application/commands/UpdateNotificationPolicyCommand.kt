package com.vitahealth.tata.preferences.application.commands

import com.vitahealth.tata.preferences.domain.model.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.NotificationPolicy

data class UpdateNotificationPolicyCommand(
    val userId: String,
    val policy: NotificationPolicy,
    val contacts: List<EmergencyContact>,
)
