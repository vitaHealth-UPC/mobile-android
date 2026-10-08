package com.vitahealth.tata.preferences.application.commands

data class UpdateHighContrastCommand(
    val userId: String,
    val enabled: Boolean,
)
