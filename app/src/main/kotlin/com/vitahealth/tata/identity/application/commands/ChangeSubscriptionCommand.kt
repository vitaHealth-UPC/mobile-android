package com.vitahealth.tata.identity.application.commands

data class ChangeSubscriptionCommand(
    val accountId: String,
    val planCode: String,
)
