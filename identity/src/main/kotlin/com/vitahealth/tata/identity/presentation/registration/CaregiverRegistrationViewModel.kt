package com.vitahealth.tata.identity.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.commands.RegisterCaregiverCommand
import com.vitahealth.tata.identity.application.commands.RequestNewVerificationCommand
import com.vitahealth.tata.identity.application.commands.VerifyCaregiverEmailCommand
import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
import com.vitahealth.tata.identity.application.handlers.RequestNewVerificationCommandHandler
import com.vitahealth.tata.identity.application.handlers.VerifyCaregiverEmailCommandHandler
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CaregiverRegistrationViewModel(
    private val registerHandler: RegisterCaregiverCommandHandler,
    private val verifyHandler: VerifyCaregiverEmailCommandHandler,
    private val requestNewVerificationHandler: RequestNewVerificationCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(CaregiverRegistrationUiState())
    val state: StateFlow<CaregiverRegistrationUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, errorMessage = null, errorCode = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null, errorCode = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null, errorCode = null) }
    fun onVerificationCodeChange(value: String) =
        _state.update { it.copy(verificationCode = value.filter(Char::isDigit).take(6), errorMessage = null, errorCode = null) }

    fun createAccount() {
        val current = _state.value
        if (current.isLoading || current.step != RegistrationStep.Account) return
        _state.update { it.copy(isLoading = true, errorMessage = null, errorCode = null) }
        viewModelScope.launch {
            when (val result = registerHandler(
                RegisterCaregiverCommand(current.name, current.email, current.password),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        accountId = result.value.id,
                        step = RegistrationStep.Verification,
                        verificationCode = "",
                    )
                }
                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = registrationMessage(result),
                        errorCode = result.code,
                    )
                }
            }
        }
    }

    fun verifyEmail() {
        val current = _state.value
        if (current.isLoading || current.step != RegistrationStep.Verification) return
        _state.update { it.copy(isLoading = true, errorMessage = null, errorCode = null) }
        viewModelScope.launch {
            when (val result = verifyHandler(
                VerifyCaregiverEmailCommand(current.email, current.verificationCode),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        accountId = result.value.id,
                        step = RegistrationStep.Complete,
                    )
                }
                is AppResult.Failure -> {
                    if (result.code == "VERIFICATION_EXPIRED") {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                step = RegistrationStep.VerificationExpired,
                                errorMessage = null, errorCode = null,
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = verificationMessage(result),
                                errorCode = result.code,
                            )
                        }
                    }
                }
            }
        }
    }

    fun requestNewVerification() {
        val current = _state.value
        if (current.isLoading || current.step != RegistrationStep.VerificationExpired) return
        _state.update { it.copy(isLoading = true, errorMessage = null, errorCode = null) }
        viewModelScope.launch {
            when (val result = requestNewVerificationHandler(
                RequestNewVerificationCommand(current.email),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        accountId = result.value.id,
                        step = RegistrationStep.Verification,
                        verificationCode = "",
                    )
                }
                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = requestVerificationMessage(result),
                        errorCode = result.code,
                    )
                }
            }
        }
    }

    private fun registrationMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "DUPLICATE_EMAIL" -> "Este correo ya está registrado."
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            else -> "No pudimos crear la cuenta."
        }

    private fun verificationMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "INVALID_VERIFICATION" -> "El código de verificación no es válido."
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            else -> failure.message
        }

    private fun requestVerificationMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            "ACCOUNT_NOT_FOUND" -> "No encontramos la cuenta asociada a este correo."
            else -> "No pudimos enviar un nuevo código."
        }

    class Factory(
        private val registerHandler: RegisterCaregiverCommandHandler,
        private val verifyHandler: VerifyCaregiverEmailCommandHandler,
        private val requestNewVerificationHandler: RequestNewVerificationCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CaregiverRegistrationViewModel(
                registerHandler = registerHandler,
                verifyHandler = verifyHandler,
                requestNewVerificationHandler = requestNewVerificationHandler,
            ) as T
    }
}
