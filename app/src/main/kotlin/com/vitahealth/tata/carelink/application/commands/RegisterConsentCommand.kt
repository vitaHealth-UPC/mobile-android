package com.vitahealth.tata.carelink.application.commands

/** Records the adult's acceptance or rejection for a specific link. */
data class RegisterConsentCommand(val careLinkId: String, val accepted: Boolean)
