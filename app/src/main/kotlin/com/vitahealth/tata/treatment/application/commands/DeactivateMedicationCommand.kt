package com.vitahealth.tata.treatment.application.commands

/** Requests medication deactivation while retaining its history. */
data class DeactivateMedicationCommand(val caregiverId: String, val medicationId: String)
