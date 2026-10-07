package com.vitahealth.tata.treatment.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.RegisterMedicationCommand
import com.vitahealth.tata.treatment.application.handlers.RegisterMedicationCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicationRegistrationViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    private val registerMedicationHandler: RegisterMedicationCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(
        MedicationRegistrationUiState(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
        ),
    )
    val state: StateFlow<MedicationRegistrationUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = updateField { copy(name = value) }
    fun onPresentationChange(value: String) = updateField { copy(presentation = value) }
    fun onFrequencyChange(value: String) = updateField { copy(frequency = value) }
    fun onTimingChange(value: String) = updateField { copy(timing = value) }
    fun onNotesChange(value: String) = updateField { copy(notes = value) }

    fun registerMedication() {
        val current = _state.value
        if (current.isLoading || current.registeredMedication != null) return

        if (current.name.isBlank() || current.presentation.isBlank()) {
            _state.update {
                it.copy(errorMessage = "Completa el nombre y la dosis/presentación antes de continuar.")
            }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = registerMedicationHandler(
                RegisterMedicationCommand(
                    caregiverId = current.caregiverId,
                    olderAdultId = current.olderAdultId,
                    name = current.name,
                    presentation = current.presentation,
                ),
            )) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        registeredMedication = result.value,
                        errorMessage = null,
                    )
                }

                is AppResult.Failure -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = medicationMessage(result),
                    )
                }
            }
        }
    }

    private fun updateField(transform: MedicationRegistrationUiState.() -> MedicationRegistrationUiState) {
        _state.update { state ->
            if (state.registeredMedication != null) state
            else state.transform().copy(errorMessage = null)
        }
    }

    private fun medicationMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "REQUIRED_FIELDS_MISSING", "REQUEST_VALIDATION_FAILED" ->
                "Completa el nombre y la dosis/presentación antes de continuar."
            "CARE_LINK_NOT_AUTHORIZED" ->
                "El vínculo de cuidado ya no está activo. Vuelve a vincular al adulto mayor."
            "OLDER_ADULT_NOT_FOUND" ->
                "No encontramos el perfil del adulto mayor."
            "NETWORK_UNAVAILABLE" ->
                "No hay conexión. Inténtalo nuevamente."
            else -> failure.message
        }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val registerMedicationHandler: RegisterMedicationCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MedicationRegistrationViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                registerMedicationHandler = registerMedicationHandler,
            ) as T
    }
}
