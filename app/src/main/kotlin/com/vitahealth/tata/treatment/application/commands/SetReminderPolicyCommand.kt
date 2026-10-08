package com.vitahealth.tata.treatment.application.commands

data class SetReminderPolicyCommand(
    val followUpDelayMinutes: String,
)
