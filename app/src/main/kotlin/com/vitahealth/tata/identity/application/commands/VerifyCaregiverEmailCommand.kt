package com.vitahealth.tata.identity.application.commands

data class VerifyCaregiverEmailCommand(
    val email: String,
    val code: String,
)
