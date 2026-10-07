package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetScheduleInstructionsCommand
import com.vitahealth.tata.treatment.application.handlers.SetScheduleInstructionsCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TreatmentScheduleInstructionsViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    medicationId: String,
    medicationLabel: String,
    treatmentId: String,
    treatmentName: String,
    dosage: String,
    frequency: String,
    private val handler: SetScheduleInstructionsCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(
        TreatmentScheduleInstructionsUiState(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
            treatmentId = treatmentId,
            treatmentName = treatmentName,
            dosage = dosage,
            frequency = frequency,
        ),
    )
    val state: StateFlow<TreatmentScheduleInstructionsUiState> = _state.asStateFlow()

    fun onScheduleChange(value: String) {
        _state.update { it.copy(scheduleText = value, validatedSchedule = null, errorMessage = null) }
    }

    fun onInstructionsChange(value: String) {
        _state.update { it.copy(instructions = value, validatedSchedule = null, errorMessage = null) }
    }

    fun continueConfiguration() {
        val current = _state.value
        when (val result = handler(
            SetScheduleInstructionsCommand(
                scheduleText = current.scheduleText,
                instructions = current.instructions,
            ),
        )) {
            is AppResult.Success -> _state.update {
                it.copy(validatedSchedule = result.value, errorMessage = null)
            }
            is AppResult.Failure -> _state.update {
                it.copy(validatedSchedule = null, errorMessage = result.message)
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
        private val dosage: String,
        private val frequency: String,
        private val handler: SetScheduleInstructionsCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreatmentScheduleInstructionsViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                dosage = dosage,
                frequency = frequency,
                handler = handler,
            ) as T
    }
}
