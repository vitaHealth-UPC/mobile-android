package com.vitahealth.tata.carelink.infrastructure.remote

/** Identifies the caregiver and adult requesting a temporary linking code. */
data class GenerateCodeRequest(
    val caregiverId: String,
    val olderAdultId: String
)
