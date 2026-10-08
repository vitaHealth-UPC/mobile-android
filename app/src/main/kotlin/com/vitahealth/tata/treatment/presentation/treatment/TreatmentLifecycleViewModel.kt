package com.vitahealth.tata.treatment.presentation.treatment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommand
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommand
import com.vitahealth.tata.treatment.application.commands.TreatmentLifecycleAction
import com.vitahealth.tata.treatment.application.handlers.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.handlers.ConfigureTreatmentCommandHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TreatmentLifecycleViewModel(
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
    reminderDelayMinutes: Int,
    private val configureTreatmentHandler: ConfigureTreatmentCommandHandler,
    private val changeStatusHandler: ChangeTreatmentStatusCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(
        TreatmentLifecycleUiState(
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
            reminderDelayMinutes = reminderDelayMinutes,
        ),
    )
    val state: StateFlow<TreatmentLifecycleUiState> = _state.asStateFlow()

    fun activate() {
        val current = _state.value
        if (current.isLoading || current.status != com.vitahealth.tata.treatment.domain.model.TreatmentStatus.DRAFT) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val configured = configureTreatmentHandler(
                ConfigureTreatmentCommand(
                    caregiverId = current.caregiverId,
                    treatmentId = current.treatmentId,
                    medicationId = current.medicationId,
                    dose = current.dosage,
                    frequency = current.frequency,
                    scheduledTimes = current.scheduleText.split(","),
                    instructions = current.instructions,
                    reminderLeadMinutes = current.reminderDelayMinutes,
                ),
            )
            if (configured is AppResult.Failure) {
                _state.update { it.copy(isLoading = false, errorMessage = lifecycleMessage(configured)) }
                return@launch
            }

            applyStatus(TreatmentLifecycleAction.ACTIVATE)
        }
    }

    fun pause() = changeStatus(TreatmentLifecycleAction.PAUSE)

    fun resume() = changeStatus(TreatmentLifecycleAction.RESUME)

    private fun changeStatus(action: TreatmentLifecycleAction) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch { applyStatus(action) }
    }

    private suspend fun applyStatus(action: TreatmentLifecycleAction) {
        val current = _state.value
        when (val result = changeStatusHandler(
            ChangeTreatmentStatusCommand(
                caregiverId = current.caregiverId,
                treatmentId = current.treatmentId,
                action = action,
            ),
        )) {
            is AppResult.Success -> _state.update {
                it.copy(
                    status = result.value.status,
                    isLoading = false,
                    errorMessage = null,
                )
            }
            is AppResult.Failure -> _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = lifecycleMessage(result),
                )
            }
        }
    }

    private fun lifecycleMessage(failure: AppResult.Failure): String =
        when (failure.code) {
            "CARE_LINK_NOT_AUTHORIZED" ->
                "El vínculo de cuidado ya no está activo."
            "RESOURCE_NOT_FOUND" ->
                "No encontramos el tratamiento solicitado."
            "CONFLICT" ->
                "El tratamiento no puede cambiar a ese estado."
            "NETWORK_UNAVAILABLE" ->
                "No hay conexión. Inténtalo nuevamente."
            "INVALID_TREATMENT_CONFIGURATION" ->
                "Completa una pauta válida antes de activar el tratamiento."
            else -> failure.message
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
        private val reminderDelayMinutes: Int,
        private val configureTreatmentHandler: ConfigureTreatmentCommandHandler,
        private val changeStatusHandler: ChangeTreatmentStatusCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreatmentLifecycleViewModel(
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
                reminderDelayMinutes = reminderDelayMinutes,
                configureTreatmentHandler = configureTreatmentHandler,
                changeStatusHandler = changeStatusHandler,
            ) as T
    }
}
