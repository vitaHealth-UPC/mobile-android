package com.vitahealth.tata.treatment.domain.model

data class Medication(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val presentation: String,
    val active: Boolean,
)
