package com.vitahealth.tata.inventory.application.commands

/** US-40: define the initial stock of a medication and its replenishment threshold. */
data class RegisterInitialInventoryCommand(
    val medicationId: String,
    val initialQuantity: Int,
    val replenishmentThreshold: Int,
)
