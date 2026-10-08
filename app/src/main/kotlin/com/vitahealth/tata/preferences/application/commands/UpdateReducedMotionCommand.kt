package com.vitahealth.tata.preferences.application.commands

data class UpdateReducedMotionCommand(
    val userId: String,
    val enabled: Boolean,
)
