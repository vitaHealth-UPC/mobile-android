package com.vitahealth.tata.inventory.infrastructure.remote

/** Body of US-40 "register initial inventory". */
data class RegisterInitialInventoryRequest(
    val medicationId: String,
    val initialQuantity: Int,
    val replenishmentThreshold: Int,
)
