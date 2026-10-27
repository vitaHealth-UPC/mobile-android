package com.vitahealth.tata.identity.presentation.access.viewmodel

import com.vitahealth.tata.identity.application.readmodels.SessionSubject
/**
 * Transient access form and authenticated result; credentials are never saved in restored state.
 */
data class SessionAccessUiState(
    val email: String = "",
    val password: String = "",
    val pin: String = "",
    val busy: Boolean = false,
    val error: String? = null,
    val subject: SessionSubject? = null,
    val pinSaved: Boolean = false,
)
