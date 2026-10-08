package com.vitahealth.tata.identity.application.commands

data class RegisterCaregiverCommand(
    val name: String,
    val email: String,
    val password: String,
)
