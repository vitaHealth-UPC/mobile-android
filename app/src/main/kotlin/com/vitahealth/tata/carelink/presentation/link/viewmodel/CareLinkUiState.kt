package com.vitahealth.tata.carelink.presentation.link.viewmodel

import com.vitahealth.tata.carelink.domain.model.entities.CareLink
import com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile

enum class CareLinkStep {
    Code,
    AwaitingConsent,
    Confirmed,
    Rejected,
}

data class CareLinkUiState(
    val caregiverId: String,
    val code: String = "",
    val step: CareLinkStep = CareLinkStep.Code,
    val acceptedLink: CareLink? = null,
    val olderAdult: OlderAdultProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
