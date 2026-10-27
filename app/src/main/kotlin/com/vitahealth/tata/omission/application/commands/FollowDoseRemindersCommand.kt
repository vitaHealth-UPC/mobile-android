package com.vitahealth.tata.omission.application.commands

/** The older adult signed in on this device and wants the reinforced reminders of their intakes. */
data class FollowDoseRemindersCommand(val olderAdultId: String, val olderAdultName: String)
