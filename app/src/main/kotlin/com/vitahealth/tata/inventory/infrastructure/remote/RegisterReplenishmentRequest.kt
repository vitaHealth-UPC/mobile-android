package com.vitahealth.tata.inventory.infrastructure.remote

/** Body of US-43 "register replenishment". Optional lot metadata is supported. */
data class RegisterReplenishmentRequest(val quantity: Int, val lot: String? = null)
