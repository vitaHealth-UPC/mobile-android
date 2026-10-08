package com.vitahealth.tata.preferences.application.commands

data class UpdateReadingAssistanceCommand(
    val userId: String,
    val enabled: Boolean,
)
