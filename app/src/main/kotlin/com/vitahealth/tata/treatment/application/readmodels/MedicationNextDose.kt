package com.vitahealth.tata.treatment.application.readmodels

import java.time.Instant

/** Identifies the next scheduled intake associated with a medication. */
data class MedicationNextDose(
    val intakeId: String,
    val medicationId: String,
    val scheduledAt: Instant,
)
