package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommand
import com.vitahealth.tata.treatment.application.handlers.SetDoseFrequencyCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TreatmentDoseFrequencyViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    medicationId: String,
    medicationLabel: String,
    treatmentId: String,
    treatmentName: String,
    private val handler: SetDoseFrequencyCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(
        TreatmentDoseFrequencyUiState(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
            treatmentId = treatmentId,
            treatmentName = treatmentName,
        ),
    )
    val state: StateFlow<TreatmentDoseFrequencyUiState> = _state.asStateFlow()

    fun onDosageChange(value: String) {
        _state.update { it.copy(dosage = value, validatedBasics = null, errorMessage = null) }
    }

    fun onFrequencyChange(value: String) {
        _state.update { it.copy(frequency = value, validatedBasics = null, errorMessage = null) }
    }

    fun continueConfiguration() {
        val current = _state.value
        when (val result = handler(
            SetDoseFrequencyCommand(
                dosage = current.dosage,
                frequency = current.frequency,
            ),
        )) {
            is AppResult.Success -> _state.update {
                it.copy(validatedBasics = result.value, errorMessage = null)
            }
            is AppResult.Failure -> _state.update {
                it.copy(validatedBasics = null, errorMessage = result.message)
            }
        }
    }

    class Factory(
        private val caregiverId: String,
        private val olderAdultId: String,
        private val olderAdultName: String,
        private val medicationId: String,
        private val medicationLabel: String,
        private val treatmentId: String,
        private val treatmentName: String,
        private val handler: SetDoseFrequencyCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreatmentDoseFrequencyViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                handler = handler,
            ) as T
    }
}
