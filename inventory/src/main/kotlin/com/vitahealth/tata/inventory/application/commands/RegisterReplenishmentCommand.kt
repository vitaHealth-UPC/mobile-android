package com.vitahealth.tata.inventory.application.commands

/** US-43: add units to an existing inventory. */
data class RegisterReplenishmentCommand(
    val medicationId: String,
    val quantity: Int,
)
