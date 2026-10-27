package com.vitahealth.tata.identity.application.commands

/** Carries sensitive credentials without a generated secret-revealing toString(). */
class SignInCommand(val email: String, val password: String)
