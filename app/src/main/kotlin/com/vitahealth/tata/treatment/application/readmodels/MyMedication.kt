package com.vitahealth.tata.treatment.application.readmodels

/** Combines medication details and administration information for the adult catalog. */
data class MyMedication(
    val id: String,
    val name: String,
    val presentation: String,
    val active: Boolean,
    val dose: String,
    val instructions: String,
    val scheduledTimes: List<String>,
)
