package com.vitahealth.tata.carelink.presentation.link.mappers

import com.vitahealth.tata.carelink.domain.model.valueobjects.CareLinkStatus
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkStep
import com.vitahealth.tata.shared.common.result.AppResult

/** Maps server-owned lifecycle statuses and stable error codes into presentation state. */
internal fun CareLinkStatus.toStep(): CareLinkStep =
    when (this) {
        CareLinkStatus.CONFIRMED -> CareLinkStep.Confirmed
        CareLinkStatus.REVOKED -> CareLinkStep.Rejected
        CareLinkStatus.PENDING,
        CareLinkStatus.AWAITING_CONSENT -> CareLinkStep.AwaitingConsent
    }

internal fun linkMessage(failure: AppResult.Failure): String =
    when (failure.code) {
        "INVALID_LINKING_CODE" -> "El código de vinculación no es válido."
        "LINKING_CODE_EXPIRED_OR_USED" -> "El código venció o ya fue utilizado."
        "CONSENT_REQUIRED" -> "Verifica tu cuenta antes de vincular a un familiar."
        "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
        else -> failure.message
    }

internal fun consentMessage(failure: AppResult.Failure): String =
    when (failure.code) {
        "CONSENT_REQUIRED" -> "No se pudo registrar el consentimiento."
        "CARE_LINK_NOT_FOUND" -> "La solicitud de vinculación ya no está disponible."
        "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
        else -> failure.message
    }

internal fun profileMessage(failure: AppResult.Failure): String =
    when (failure.code) {
        "NETWORK_UNAVAILABLE" -> "La solicitud fue enviada, pero no pudimos cargar el perfil."
        else -> failure.message
    }
