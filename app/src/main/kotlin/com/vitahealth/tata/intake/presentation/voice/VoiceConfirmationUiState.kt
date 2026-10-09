package com.vitahealth.tata.intake.presentation.voice

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.presentation.detail.DoseDetailUiState

enum class VoicePhase {
    LOADING,
    READY,
    RECORDING,
    PROCESSING,
    NOT_RECOGNIZED,
    UNAVAILABLE,
    PERMISSION_DENIED,
    CONFIRMED,
}

data class VoiceConfirmationUiState(
    val phase: VoicePhase = VoicePhase.LOADING,
    val dose: DoseDetailReadModel? = null,
    val confirmation: DoseDetailUiState.Content? = null,
)
