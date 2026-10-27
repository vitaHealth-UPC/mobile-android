package com.vitahealth.tata.identity.application.commands

/** Requests the selected plan for an existing account. */
data class ChangeSubscriptionCommand(val accountId: String, val planCode: String)
