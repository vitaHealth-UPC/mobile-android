package com.vitahealth.tata.identity.presentation.registration

enum class RegistrationStep {
    Account,
    Verification,
    VerificationExpired,
    Complete,
}

data class CaregiverRegistrationUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val verificationCode: String = "",
    val accountId: String? = null,
    val step: RegistrationStep = RegistrationStep.Account,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
