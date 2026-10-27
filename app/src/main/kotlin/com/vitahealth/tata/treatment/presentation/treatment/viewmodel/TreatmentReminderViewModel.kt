package com.vitahealth.tata.treatment.presentation.treatment.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommand
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Validates the entered follow-up delay without changing its numeric meaning. */
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
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            TreatmentReminderUiState(
                caregiverId = caregiverId,
                olderAdultId = olderAdultId,
                olderAdultName = olderAdultName,
                medicationId = medicationId,
                medicationLabel = medicationLabel,
                treatmentId = treatmentId,
                treatmentName = treatmentName,
                followUpDelayMinutes = savedState["followUpDelayMinutes"] ?: "10",
                dosage = dosage,
                frequency = frequency,
                scheduleText = scheduleText,
                instructions = instructions,
            )
        )
    val state: StateFlow<TreatmentReminderUiState> = _state.asStateFlow()

    fun onDelayChange(value: String) {
        savedState["followUpDelayMinutes"] = value
        _state.update {
            it.copy(followUpDelayMinutes = value, validatedPolicy = null, errorMessage = null)
        }
    }

    /** A back-stack return keeps inputs but never replays a completed navigation. */
    fun consumeCompletion() {
        _state.update { it.copy(validatedPolicy = null) }
    }

    fun continueConfiguration() {
        val current = _state.value
        when (
            val result =
                handler(
                    SetReminderPolicyCommand(followUpDelayMinutes = current.followUpDelayMinutes)
                )
        ) {
            is AppResult.Success ->
                _state.update { it.copy(validatedPolicy = result.value, errorMessage = null) }
            is AppResult.Failure ->
                _state.update { it.copy(validatedPolicy = null, errorMessage = result.message) }
        }
    }

    /** Creates the reminder-step model only for compatible ViewModel requests. */
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
        ): T {
            require(modelClass.isAssignableFrom(TreatmentReminderViewModel::class.java)) {
                "Unsupported ViewModel: ${modelClass.name}"
            }
            return TreatmentReminderViewModel(
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
                savedState = savedState,
            )
                as T
        }
    }
}
