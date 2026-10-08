package com.vitahealth.tata.omission.application.commands

/** The caregiver opened the follow-up of an older adult and wants its missed-dose alerts. */
data class FollowCaregiverAlertsCommand(
    val caregiverId: String,
    val olderAdultId: String,
)

/** The older adult signed in on this device and wants the reinforced reminders of their intakes. */
data class FollowDoseRemindersCommand(
    val olderAdultId: String,
    val olderAdultName: String,
)
