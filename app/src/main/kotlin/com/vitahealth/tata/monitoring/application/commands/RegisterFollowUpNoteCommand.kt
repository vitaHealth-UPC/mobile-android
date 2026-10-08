package com.vitahealth.tata.monitoring.application.commands

data class RegisterFollowUpNoteCommand(
    val caregiverId: String,
    val olderAdultId: String,
    val text: String,
)
