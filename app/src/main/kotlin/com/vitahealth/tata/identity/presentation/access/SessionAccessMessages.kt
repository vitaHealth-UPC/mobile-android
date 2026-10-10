package com.vitahealth.tata.identity.presentation.access

/**
 * Maps backend / ViewModel error codes to Spanish copy for session access.
 * Pure function so PIN and email login share one source of truth and stay testable.
 */
internal fun sessionAccessMessage(code: String): String = when (code) {
    "PIN_LOCKED", "PIN_TEMPORARILY_BLOCKED" ->
        "Tu PIN está bloqueado temporalmente. Intenta nuevamente en 15 minutos."
    "INVALID_PIN", "INVALID_CREDENTIALS", "PIN_INCORRECT" ->
        "Los datos de acceso no son correctos. Inténtalo otra vez."
    "ACCOUNT_NOT_ACTIVE", "CONSENT_REQUIRED" ->
        "Verifica tu cuenta y completa la vinculación para continuar."
    "NETWORK_UNAVAILABLE" ->
        "No hay conexión. Revisa tu red e inténtalo otra vez."
    "REQUIRED_FIELDS" ->
        "Completa tu correo y contraseña."
    else ->
        "No pudimos completar el acceso. Inténtalo nuevamente."
}
