package com.vitahealth.tata.carelink.presentation.link.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommand
import com.vitahealth.tata.carelink.application.commands.AcceptCareLinkCommandHandler
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQuery
import com.vitahealth.tata.carelink.application.queries.GetOlderAdultProfileQueryHandler
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommand
import com.vitahealth.tata.carelink.application.commands.RegisterConsentCommandHandler
import com.vitahealth.tata.carelink.presentation.link.mappers.toStep
import com.vitahealth.tata.carelink.presentation.link.mappers.linkMessage
import com.vitahealth.tata.carelink.presentation.link.mappers.consentMessage
import com.vitahealth.tata.carelink.presentation.link.mappers.profileMessage
import com.vitahealth.tata.shared.common.result.AppResult
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Coordinates code submission, consent and the profile belonging to the active request. */
class CareLinkViewModel(
    caregiverId: String,
    private val acceptCareLinkHandler: AcceptCareLinkCommandHandler,
    private val registerConsentHandler: RegisterConsentCommandHandler,
    private val getOlderAdultHandler: GetOlderAdultProfileQueryHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(CareLinkUiState(caregiverId = caregiverId))
    val state: StateFlow<CareLinkUiState> = _state.asStateFlow()
    private var previewJob: Job? = null

    fun onCodeChange(value: String) {
        val normalized =
            value.uppercase(Locale.ROOT).filter { it.isLetterOrDigit() || it == '-' }.take(16)
        _state.update { it.copy(code = normalized, errorMessage = null) }
    }

    fun previewOlderAdult(olderAdultId: String) {
        if (_state.value.step != CareLinkStep.Code || _state.value.isLoading) return
        previewJob?.cancel()
        previewJob = viewModelScope.launch { loadOlderAdult(olderAdultId) }
    }

    fun sendLinkRequest() {
        val current = _state.value
        if (current.isLoading || current.step != CareLinkStep.Code) return
        if (current.code.isBlank()) {
            _state.update { it.copy(errorMessage = "Ingresa el código temporal.") }
            return
        }

        previewJob?.cancel()
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (
                val result =
                    acceptCareLinkHandler(
                        AcceptCareLinkCommand(
                            caregiverId = current.caregiverId,
                            code = current.code,
                        )
                    )
            ) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            step = result.value.status.toStep(),
                            acceptedLink = result.value,
                        )
                    }
                    loadOlderAdult(result.value.olderAdultId)
                }

                is AppResult.Failure ->
                    _state.update { it.copy(isLoading = false, errorMessage = linkMessage(result)) }
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
            when (
                val result =
                    registerConsentHandler(
                        RegisterConsentCommand(careLinkId = careLinkId, accepted = accepted)
                    )
            ) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            step = result.value.status.toStep(),
                            acceptedLink = result.value,
                        )
                    }

                is AppResult.Failure ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = consentMessage(result))
                    }
            }
        }
    }

    private suspend fun loadOlderAdult(olderAdultId: String) {
        when (val result = getOlderAdultHandler(GetOlderAdultProfileQuery(olderAdultId))) {
            is AppResult.Success -> _state.update { it.copy(olderAdult = result.value) }

            is AppResult.Failure -> _state.update { it.copy(errorMessage = profileMessage(result)) }
        }
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
            )
                as T
    }
}
