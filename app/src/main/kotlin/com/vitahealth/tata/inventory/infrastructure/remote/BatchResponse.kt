package com.vitahealth.tata.inventory.infrastructure.remote

/** Mirrors the backend `BatchResource`: quantity, timestamp and optional lot metadata. */
data class BatchResponse(
    val id: String,
    val quantity: Int,
    val registeredAt: String,
    val lot: String? = null,
)
