package com.vitahealth.tata.intake.presentation.detail

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel

/** Result of confirming a dose, as the backend decided it. */
enum class ConfirmationOutcome {
    /** Confirmed on time. */
    CONFIRMED,

    /** A confirmation attempt returned the existing record without another transition. */
    ALREADY_CONFIRMED,

    /** Confirmed within the tolerance period (US-23). */
    LATE,

    /** The tolerance period had ended: the omission stays and the confirmation is not recorded (US-23, 409). */
    OMISSION_PRESERVED,
}

sealed interface DoseDetailUiState {
    data object Loading : DoseDetailUiState
    /** [outcome] is set once a confirmation attempt ends with a result screen (US-06 / US-23). */
    data class Content(
        val dose: DoseDetailReadModel,
        val nextDose: com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel? = null,
        val confirming: Boolean = false,
        val confirmationMessage: String? = null,
        val confirmationSucceeded: Boolean = false,
        val outcome: ConfirmationOutcome? = null,
        val confirmationUnavailable: Boolean = false,
    ) : DoseDetailUiState
    data class Error(val message: String) : DoseDetailUiState
}
