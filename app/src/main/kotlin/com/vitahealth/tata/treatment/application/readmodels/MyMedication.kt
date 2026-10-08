package com.vitahealth.tata.treatment.application.readmodels

import java.time.Instant

data class MyMedication(
    val id: String,
    val name: String,
    val presentation: String,
    val active: Boolean,
    val dose: String,
    val instructions: String,
    val scheduledTimes: List<String>,
)

data class MedicationNextDose(val intakeId: String, val medicationId: String, val scheduledAt: Instant)
