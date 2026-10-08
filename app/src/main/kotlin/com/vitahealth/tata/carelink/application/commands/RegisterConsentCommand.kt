package com.vitahealth.tata.carelink.application.commands

data class RegisterConsentCommand(
    val careLinkId: String,
    val accepted: Boolean,
)
