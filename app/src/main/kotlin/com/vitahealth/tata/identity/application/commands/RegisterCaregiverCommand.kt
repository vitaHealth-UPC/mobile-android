package com.vitahealth.tata.identity.application.commands

/** Carries the caregiver registration form to its application handler. */
data class RegisterCaregiverCommand(val name: String, val email: String, val password: String)
