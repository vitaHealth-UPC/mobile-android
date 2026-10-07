package com.vitahealth.tata.preferences.application.commands

data class UpdateVoiceConfirmationCommand(
    val userId: String,
    val enabled: Boolean,
)
