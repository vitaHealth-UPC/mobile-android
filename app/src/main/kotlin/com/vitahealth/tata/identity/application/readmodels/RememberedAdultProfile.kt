package com.vitahealth.tata.identity.application.readmodels

/** Device-local display identity for the PIN shortcut; it grants no access to clinical data. */
data class RememberedAdultProfile(val id: String, val name: String?)
