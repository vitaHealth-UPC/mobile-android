package com.vitahealth.tata.intake.application.readmodels

enum class VoiceConfirmationStatus { CONFIRMED, ALREADY_CONFIRMED, NOT_RECOGNIZED, NOT_VALIDATED, PROVIDER_UNAVAILABLE }

/** A recognized phrase alone does not prove that an intake was recorded. */
data class VoiceConfirmationReadModel(val status: VoiceConfirmationStatus, val intake: DoseDetailReadModel? = null)
