package com.vitahealth.tata.treatment.application.commands

/** Requests a lifecycle transition for one caregiver-owned treatment. */
data class ChangeTreatmentStatusCommand(
    val caregiverId: String,
    val treatmentId: String,
    val action: TreatmentLifecycleAction,
)
