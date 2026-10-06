package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.CreateTreatmentCommand
import com.vitahealth.tata.treatment.application.handlers.CreateTreatmentCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TreatmentCreationViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    medicationId: String,
    medicationLabel: String,
    private val createTreatmentHandler: CreateTreatmentCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(
        TreatmentCreationUiState(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
        ),
    )
    val state: StateFlow<TreatmentCreationUiState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.update { current ->
            if (current.createdTreatment != null) current
            else current.copy(name = value, errorMessage = null)
        }
    }

    fun createTreatment() {
        val current = _state.value
        if (current.isLoading || current.createdTreatment != null) return

        if (current.name.isBlank()) {
            _state.update { it.copy(errorMessage = "Completa el nombre del tratamiento.") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = createTreatmentHandler(
                CreateTreatmentCommand(
                    caregiverId = current.caregiverId,
                    olderAdultId = current.olderAdultId,
                    name = current.name,
                ),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        createdTreatment = result.value,
                        errorMessage = null,
                    )
                }

                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = treatmentMessage(result),
                    )
                }
            }
        }
    }

    private fun treatmentMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "REQUIRED_FIELDS_MISSING", "REQUEST_VALIDATION_FAILED" ->
                "Completa el nombre del tratamiento."
            "CARE_LINK_NOT_AUTHORIZED" ->
                "El vínculo de cuidado ya no está activo. Vuelve a vincular al adulto mayor."
            "NETWORK_UNAVAILABLE" ->
                "No hay conexión. Inténtalo nuevamente."
            else -> failure.message
        }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val medicationId: String,
        private val medicationLabel: String,
        private val createTreatmentHandler: CreateTreatmentCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreatmentCreationViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                createTreatmentHandler = createTreatmentHandler,
            ) as T
    }
}
