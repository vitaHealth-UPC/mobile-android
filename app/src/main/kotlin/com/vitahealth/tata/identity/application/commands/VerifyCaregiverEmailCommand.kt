package com.vitahealth.tata.identity.application.commands

/** Carries the email and six-digit challenge response for verification. */
data class VerifyCaregiverEmailCommand(val email: String, val code: String)
