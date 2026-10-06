package com.vitahealth.tata.identity.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.commands.RegisterCaregiverCommand
import com.vitahealth.tata.identity.application.commands.VerifyCaregiverEmailCommand
import com.vitahealth.tata.identity.application.handlers.RegisterCaregiverCommandHandler
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
) : ViewModel() {
    private val _state = MutableStateFlow(CaregiverRegistrationUiState())
    val state: StateFlow<CaregiverRegistrationUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onVerificationCodeChange(value: String) =
        _state.update { it.copy(verificationCode = value.filter(Char::isDigit).take(6), errorMessage = null) }

    fun createAccount() {
        val current = _state.value
        if (current.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = registerHandler(
                RegisterCaregiverCommand(current.name, current.email, current.password),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, step = RegistrationStep.Verification)
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun verifyEmail() {
        val current = _state.value
        if (current.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = verifyHandler(
                VerifyCaregiverEmailCommand(current.email, current.verificationCode),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, step = RegistrationStep.Complete)
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    class Factory(
        private val registerHandler: RegisterCaregiverCommandHandler,
        private val verifyHandler: VerifyCaregiverEmailCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CaregiverRegistrationViewModel(registerHandler, verifyHandler) as T
    }
}
