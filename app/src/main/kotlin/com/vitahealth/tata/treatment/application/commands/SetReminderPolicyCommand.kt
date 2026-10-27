package com.vitahealth.tata.treatment.application.commands

/** Carries the entered follow-up interval for reminder policy validation. */
data class SetReminderPolicyCommand(val followUpDelayMinutes: String)
