package com.vitahealth.tata.treatment.application.commands

enum class TreatmentLifecycleAction {
    ACTIVATE,
    PAUSE,
    RESUME,
}

data class ChangeTreatmentStatusCommand(
    val caregiverId: String,
    val treatmentId: String,
    val action: TreatmentLifecycleAction,
)
