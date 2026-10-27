package com.vitahealth.tata.identity.presentation.access.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.commands.RegisterPinCommand
import com.vitahealth.tata.identity.application.commands.RegisterPinCommandHandler
import com.vitahealth.tata.identity.application.commands.RestoreSessionCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInCommand
import com.vitahealth.tata.identity.application.commands.SignInSocialCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInCommandHandler
import com.vitahealth.tata.identity.application.commands.SignInWithPinCommand
import com.vitahealth.tata.identity.application.commands.SignInWithPinCommandHandler
import com.vitahealth.tata.identity.application.readmodels.SessionSubject
import com.vitahealth.tata.identity.presentation.access.ui.DemoCredentials
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Owns access form actions and prevents simultaneous password, PIN and restore requests. */
class SessionAccessViewModel(
    private val signInHandler: SignInCommandHandler,
    private val signInWithPinHandler: SignInWithPinCommandHandler,
    private val registerPinHandler: RegisterPinCommandHandler,
    private val restoreSessionHandler: RestoreSessionCommandHandler,
    private val signInSocialHandler: SignInSocialCommandHandler? = null,
) : ViewModel() {

    private val mutable = MutableStateFlow(SessionAccessUiState())
    val state = mutable.asStateFlow()

    /** Updates the email draft and clears the previous form error. */
    fun email(value: String) {
        if (state.value.busy) return
        mutable.update { it.copy(email = value, error = null) }
    }

    /** Keeps the password in transient memory until authentication completes. */
    fun password(value: String) {
        if (state.value.busy) return
        mutable.update { it.copy(password = value, error = null) }
    }

    /** Prefills the seeded demo caregiver for the existing demonstration flow. */
    fun useDemoAccount() {
        if (state.value.busy) return
        mutable.update {
            it.copy(
                email = DemoCredentials.EMAIL,
                password = DemoCredentials.PASSWORD,
                error = null,
            )
        }
    }

    /** Accepts one keypad digit or deletion, bounded to the four-digit PIN contract. */
    fun digit(value: String) {
        if (state.value.busy) return
        if (value != DELETE_DIGIT && (value.length != 1 || value[0] !in '0'..'9')) return
        mutable.update {
            val pin = if (value == DELETE_DIGIT) it.pin.dropLast(1) else (it.pin + value).take(4)
            it.copy(pin = pin, error = null)
        }
    }

    /** Restores the subject only when the server accepts the stored session. */
    fun restore() = authenticate(ignoreMissingSession = true) { restoreSessionHandler() }

    /** Validates required credentials before requesting caregiver access. */
    fun signIn() {
        val current = state.value
        if (current.busy) return
        if (current.email.isBlank() || current.password.isBlank()) {
            mutable.update { it.copy(error = "REQUIRED_FIELDS") }
            return
        }
        authenticate { signInHandler(SignInCommand(current.email, current.password)) }
    }

    /** Completes provider sign-in using the normal guarded, encrypted Tata session path. */
    fun socialToken(firebaseIdToken: String) {
        val handler = signInSocialHandler
        if (handler == null) {
            mutable.update { it.copy(error = "SOCIAL_UNAVAILABLE") }
            return
        }
        authenticate { handler(firebaseIdToken) }
    }

    /** Reports a provider cancellation or unavailable configuration without creating a session. */
    fun socialError(code: String) {
        if (state.value.busy) return
        mutable.update { it.copy(error = code) }
    }

    /** Registers a new PIN after consent, or authenticates an existing older-adult PIN. */
    fun pin(olderAdultId: String, setup: Boolean) {
        val current = state.value
        if (current.busy) return
        if (!current.pin.matches(Regex("[0-9]{4}"))) {
            mutable.update { it.copy(error = "INVALID_PIN") }
            return
        }
        if (!setup) {
            authenticate { signInWithPinHandler(SignInWithPinCommand(olderAdultId, current.pin)) }
            return
        }
        if (current.busy) return
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = registerPinHandler(RegisterPinCommand(olderAdultId, current.pin))
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    mutable.update { it.copy(busy = false, pin = "", pinSaved = true) }
                is AppResult.Failure ->
                    mutable.update { it.copy(busy = false, pin = "", error = result.code) }
            }
        }
    }

    private fun authenticate(
        ignoreMissingSession: Boolean = false,
        call: suspend () -> AppResult<SessionSubject>,
    ) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            val result = call()
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    mutable.update {
                        it.copy(busy = false, password = "", pin = "", subject = result.value)
                    }
                is AppResult.Failure ->
                    mutable.update {
                        it.copy(
                            busy = false,
                            pin = if (result.code in PIN_RETRY_CODES) it.pin else "",
                            error =
                                if (
                                    ignoreMissingSession && result.code == "AUTHENTICATION_REQUIRED"
                                )
                                    null
                                else result.code,
                        )
                    }
            }
        }
    }

    /** Supplies use-case handlers to the screen-scoped ViewModel. */
    class Factory(
        private val signIn: SignInCommandHandler,
        private val signInWithPin: SignInWithPinCommandHandler,
        private val registerPin: RegisterPinCommandHandler,
        private val restoreSession: RestoreSessionCommandHandler,
        private val signInSocial: SignInSocialCommandHandler? = null,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SessionAccessViewModel::class.java))
            return SessionAccessViewModel(signIn, signInWithPin, registerPin, restoreSession, signInSocial) as T
        }
    }

    private companion object {
        const val DELETE_DIGIT = "⌫"
        val PIN_RETRY_CODES =
            setOf(
                "INVALID_PIN",
                "INVALID_CREDENTIALS",
                "PIN_INCORRECT",
                "PIN_LOCKED",
                "PIN_TEMPORARILY_BLOCKED",
            )
    }
}
