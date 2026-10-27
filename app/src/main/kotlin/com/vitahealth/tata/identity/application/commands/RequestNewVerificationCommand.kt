package com.vitahealth.tata.identity.application.commands

/** Requests another email verification challenge for the account. */
data class RequestNewVerificationCommand(val email: String)
