package com.vitahealth.tata.treatment.domain.model

data class Treatment(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val status: TreatmentStatus,
)
