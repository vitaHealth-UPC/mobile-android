package com.vitahealth.tata.carelink.presentation.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommand
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommand
import com.vitahealth.tata.carelink.application.handlers.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.handlers.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.application.handlers.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQuery
import com.vitahealth.tata.carelink.domain.model.CareLinkStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CareLinkViewModel(
    caregiverId: String,
    private val acceptCareLinkHandler: AcceptCareLinkCommandHandler,
    private val registerConsentHandler: RegisterConsentCommandHandler,
    private val getOlderAdultHandler: GetOlderAdultProfileQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(CareLinkUiState(caregiverId = caregiverId))
    val state: StateFlow<CareLinkUiState> = _state.asStateFlow()

    fun onCodeChange(value: String) {
        val normalized = value
            .uppercase()
            .filter { it.isLetterOrDigit() || it == '-' }
            .take(16)
        _state.update { it.copy(code = normalized, errorMessage = null) }
    }

    fun previewOlderAdult(olderAdultId: String) {
        if (_state.value.step == CareLinkStep.Code) viewModelScope.launch { loadOlderAdult(olderAdultId) }
    }

    fun sendLinkRequest() {
        val current = _state.value
        if (current.isLoading || current.step != CareLinkStep.Code) return
        if (current.code.isBlank()) {
            _state.update { it.copy(errorMessage = "Ingresa el código temporal.") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = acceptCareLinkHandler(
                AcceptCareLinkCommand(
                    caregiverId = current.caregiverId,
                    code = current.code,
                ),
            )) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            step = CareLinkStep.AwaitingConsent,
                            acceptedLink = result.value,
                        )
                    }
                    loadOlderAdult(result.value.olderAdultId)
                }

                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = linkMessage(result),
                    )
                }
            }
        }
    }

    fun acceptConsent() = recordConsent(accepted = true)

    fun rejectConsent() = recordConsent(accepted = false)

    private fun recordConsent(accepted: Boolean) {
        val current = _state.value
        val careLinkId = current.acceptedLink?.id ?: return
        if (current.isLoading || current.step != CareLinkStep.AwaitingConsent) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = registerConsentHandler(
                RegisterConsentCommand(
                    careLinkId = careLinkId,
                    accepted = accepted,
                ),
            )) {
                is AppResult.Success -> _state.update {
                    val nextStep = when (result.value.status) {
                        CareLinkStatus.CONFIRMED -> CareLinkStep.Confirmed
                        CareLinkStatus.REVOKED -> CareLinkStep.Rejected
                        else -> CareLinkStep.AwaitingConsent
                    }
                    it.copy(
                        isLoading = false,
                        step = nextStep,
                        acceptedLink = result.value,
                    )
                }

                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = consentMessage(result),
                    )
                }
            }
        }
    }

    private suspend fun loadOlderAdult(olderAdultId: String) {
        when (val result = getOlderAdultHandler(GetOlderAdultProfileQuery(olderAdultId))) {
            is AppResult.Success -> _state.update {
                it.copy(olderAdult = result.value)
            }

            is AppResult.Failure -> _state.update {
                it.copy(errorMessage = profileMessage(result))
            }
        }
    }

    private fun linkMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "INVALID_LINKING_CODE" -> "El código de vinculación no es válido."
            "LINKING_CODE_EXPIRED_OR_USED" -> "El código venció o ya fue utilizado."
            "CONSENT_REQUIRED" -> "Verifica tu cuenta antes de vincular a un familiar."
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            else -> failure.message
        }

    private fun consentMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "CONSENT_REQUIRED" -> "No se pudo registrar el consentimiento."
            "CARE_LINK_NOT_FOUND" -> "La solicitud de vinculación ya no está disponible."
            "NETWORK_UNAVAILABLE" -> "No hay conexión. Inténtalo nuevamente."
            else -> failure.message
        }

    private fun profileMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "NETWORK_UNAVAILABLE" -> "La solicitud fue enviada, pero no pudimos cargar el perfil."
            else -> failure.message
        }

    class Factory(
        private val caregiverId: String,
        private val acceptCareLinkHandler: AcceptCareLinkCommandHandler,
        private val registerConsentHandler: RegisterConsentCommandHandler,
        private val getOlderAdultHandler: GetOlderAdultProfileQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CareLinkViewModel(
                caregiverId = caregiverId,
                acceptCareLinkHandler = acceptCareLinkHandler,
                registerConsentHandler = registerConsentHandler,
                getOlderAdultHandler = getOlderAdultHandler,
            ) as T
    }
}
