package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommand
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Validates dose and frequency while preserving the treatment wizard context. */
class TreatmentDoseFrequencyViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    medicationId: String,
    medicationLabel: String,
    treatmentId: String,
    treatmentName: String,
    private val handler: SetDoseFrequencyCommandHandler,
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            TreatmentDoseFrequencyUiState(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                dosage = savedState["dosage"] ?: "",
                frequency = savedState["frequency"] ?: "",
            )
        )
    val state: StateFlow<TreatmentDoseFrequencyUiState> = _state.asStateFlow()

    fun onDosageChange(value: String) {
        savedState["dosage"] = value
        _state.update { it.copy(dosage = value, validatedBasics = null, errorMessage = null) }
    }

    fun onFrequencyChange(value: String) {
        savedState["frequency"] = value
        _state.update { it.copy(frequency = value, validatedBasics = null, errorMessage = null) }
    }

    /** A back-stack return keeps inputs but never replays a completed navigation. */
    fun initializeFrequency(value: String) {
        if (!savedState.contains("frequency") && value.isNotBlank()) onFrequencyChange(value)
    }

    fun consumeCompletion() {
        _state.update { it.copy(validatedBasics = null) }
    }

    fun continueConfiguration() {
        val current = _state.value
        when (
            val result =
                handler(
                    SetDoseFrequencyCommand(dosage = current.dosage, frequency = current.frequency)
                )
        ) {
            is AppResult.Success ->
                _state.update { it.copy(validatedBasics = result.value, errorMessage = null) }
            is AppResult.Failure ->
                _state.update { it.copy(validatedBasics = null, errorMessage = result.message) }
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
        private val draftState: SavedStateHandle? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            createWithState(modelClass, draftState ?: SavedStateHandle())

        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
            createWithState(modelClass, draftState ?: extras.createSavedStateHandle())

        @Suppress("UNCHECKED_CAST")
        private fun <T : ViewModel> createWithState(
            modelClass: Class<T>,
            savedState: SavedStateHandle,
        ): T =
            TreatmentDoseFrequencyViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                handler = handler,
                savedState = savedState,
            )
                as T
    }
}
