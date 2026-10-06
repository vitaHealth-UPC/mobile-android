package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommand
import com.vitahealth.tata.treatment.application.handlers.SetReminderPolicyCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TreatmentReminderViewModel(
    caregiverId: String,
    olderAdultId: String,
    olderAdultName: String,
    medicationId: String,
    medicationLabel: String,
    treatmentId: String,
    treatmentName: String,
    dosage: String,
    frequency: String,
    scheduleText: String,
    instructions: String,
    private val handler: SetReminderPolicyCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(
        TreatmentReminderUiState(
            caregiverId = caregiverId,
            olderAdultId = olderAdultId,
            olderAdultName = olderAdultName,
            medicationId = medicationId,
            medicationLabel = medicationLabel,
            treatmentId = treatmentId,
            treatmentName = treatmentName,
            dosage = dosage,
            frequency = frequency,
            scheduleText = scheduleText,
            instructions = instructions,
        ),
    )
    val state: StateFlow<TreatmentReminderUiState> = _state.asStateFlow()

    fun onDelayChange(value: String) {
        _state.update {
            it.copy(
                followUpDelayMinutes = value.filter(Char::isDigit).take(4),
                validatedPolicy = null,
                errorMessage = null,
            )
        }
    }

    fun continueConfiguration() {
        val current = _state.value
        when (val result = handler(
            SetReminderPolicyCommand(
                followUpDelayMinutes = current.followUpDelayMinutes,
            ),
        )) {
            is AppResult.Success -> _state.update {
                it.copy(validatedPolicy = result.value, errorMessage = null)
            }
            is AppResult.Failure -> _state.update {
                it.copy(validatedPolicy = null, errorMessage = result.message)
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
        private val scheduleText: String,
        private val instructions: String,
        private val handler: SetReminderPolicyCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreatmentReminderViewModel(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                dosage = dosage,
                frequency = frequency,
                scheduleText = scheduleText,
                instructions = instructions,
                handler = handler,
            ) as T
    }
}
