package com.vitahealth.tata.identity.presentation.recovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.ports.PasswordRecovery
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RecoveryStep {
    Email,
    Code,
    Complete,
}

data class PasswordRecoveryUiState(
    val step: RecoveryStep = RecoveryStep.Email,
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: String? = null,
) {
    override fun toString() =
        "PasswordRecoveryUiState(step=$step, busy=$busy, error=$error, credentials=REDACTED)"
}

/** Holds secrets only in transient memory and never authenticates after recovery. */
class PasswordRecoveryViewModel(private val recovery: PasswordRecovery) : ViewModel() {
    private val mutable = MutableStateFlow(PasswordRecoveryUiState())
    val state = mutable.asStateFlow()

    fun email(value: String) {
        if (!state.value.busy && state.value.step == RecoveryStep.Email)
            mutable.value = state.value.copy(email = value.take(200), error = null)
    }

    fun code(value: String) {
        if (!state.value.busy && state.value.step == RecoveryStep.Code)
            mutable.value =
                state.value.copy(code = value.filter { it in '0'..'9' }.take(6), error = null)
    }

    fun password(value: String) {
        if (!state.value.busy && state.value.step == RecoveryStep.Code)
            mutable.value = state.value.copy(password = value, error = null)
    }

    fun reset() {
        if (!state.value.busy) mutable.value = PasswordRecoveryUiState()
    }

    fun request() {
        val current = state.value
        if (current.busy || current.step != RecoveryStep.Email) return
        val email = current.email.trim()
        if (!email.matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) {
            mutable.value = current.copy(error = "INVALID_EMAIL")
            return
        }
        mutable.value = current.copy(email = email, busy = true, error = null)
        viewModelScope.launch {
            val result = recovery.request(email)
            currentCoroutineContext().ensureActive()
            mutable.value =
                when (result) {
                    is AppResult.Success ->
                        PasswordRecoveryUiState(step = RecoveryStep.Code, email = email)
                    is AppResult.Failure ->
                        mutable.value.copy(busy = false, error = result.code ?: "REQUEST_FAILED")
                }
        }
    }

    fun complete() {
        val current = state.value
        if (current.busy || current.step != RecoveryStep.Code) return
        if (
            current.code.length != 6 ||
                current.password.isBlank() ||
                current.password.length < 8 ||
                current.password.toByteArray(Charsets.UTF_8).size > 72
        ) {
            mutable.value = current.copy(error = "VALIDATION_ERROR")
            return
        }
        mutable.value = current.copy(busy = true, error = null)
        viewModelScope.launch {
            val result = recovery.complete(current.email, current.code, current.password)
            currentCoroutineContext().ensureActive()
            mutable.value =
                when (result) {
                    is AppResult.Success -> PasswordRecoveryUiState(step = RecoveryStep.Complete)
                    is AppResult.Failure ->
                        mutable.value.copy(
                            busy = false,
                            password = "",
                            error = result.code ?: "REQUEST_FAILED",
                        )
                }
        }
    }
}
