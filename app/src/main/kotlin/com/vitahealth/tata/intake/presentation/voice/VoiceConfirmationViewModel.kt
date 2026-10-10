package com.vitahealth.tata.intake.presentation.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.VoiceAudioRecorder
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.handlers.*
import com.vitahealth.tata.intake.application.queries.*
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationStatus
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.intake.presentation.detail.*
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VoiceConfirmationViewModel(
    private val intakeId: String,
    private val detail: GetDoseDetailQueryHandler,
    private val confirm: ConfirmDoseByVoiceCommandHandler,
    private val recorder: VoiceAudioRecorder,
    private val nextDose: GetNextDoseQueryHandler,
    private val context: CoroutineContext = EmptyCoroutineContext,
) : ViewModel() {
    private val mutableState = MutableStateFlow(VoiceConfirmationUiState())
    val state: StateFlow<VoiceConfirmationUiState> = mutableState.asStateFlow()
    private var recordingJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch(context) {
            mutableState.value =
                when (val result = detail(GetDoseDetailQuery(intakeId))) {
                    is AppResult.Success -> VoiceConfirmationUiState(VoicePhase.READY, result.value)
                    is AppResult.Failure -> VoiceConfirmationUiState(VoicePhase.UNAVAILABLE)
                }
        }
    }

    fun permissionDenied() {
        mutableState.value = state.value.copy(phase = VoicePhase.PERMISSION_DENIED)
    }

    fun start(language: String) {
        val current = state.value
        if (
            current.dose == null ||
                current.phase in
                    setOf(VoicePhase.RECORDING, VoicePhase.PROCESSING, VoicePhase.CONFIRMED)
        )
            return
        when (recorder.start()) {
            is AppResult.Failure ->
                mutableState.value = current.copy(phase = VoicePhase.UNAVAILABLE)
            is AppResult.Success -> {
                mutableState.value = current.copy(phase = VoicePhase.RECORDING)
                recordingJob =
                    viewModelScope.launch(context) {
                        delay(8000)
                        finish(language)
                    }
            }
        }
    }

    suspend fun finish(language: String) {
        if (state.value.phase != VoicePhase.RECORDING) return
        val audio = recorder.finish()
        if (audio is AppResult.Failure) {
            mutableState.value = state.value.copy(phase = VoicePhase.UNAVAILABLE)
            return
        }
        audio as AppResult.Success
        mutableState.value = state.value.copy(phase = VoicePhase.PROCESSING)
        when (
            val result =
                confirm(
                    ConfirmDoseByVoiceCommand(
                        intakeId,
                        audio.value.audio,
                        audio.value.contentType,
                        language,
                    )
                )
        ) {
            is AppResult.Failure ->
                mutableState.value = state.value.copy(phase = VoicePhase.UNAVAILABLE)
            is AppResult.Success -> {
                val value = result.value
                val dose = value.intake
                if (
                    dose != null &&
                        value.status in
                            setOf(
                                VoiceConfirmationStatus.CONFIRMED,
                                VoiceConfirmationStatus.ALREADY_CONFIRMED,
                            )
                ) {
                    val outcome =
                        if (value.status == VoiceConfirmationStatus.ALREADY_CONFIRMED)
                            ConfirmationOutcome.ALREADY_CONFIRMED
                        else if (dose.status == DoseStatus.LATE) ConfirmationOutcome.LATE
                        else ConfirmationOutcome.CONFIRMED
                    var content =
                        DoseDetailUiState.Content(
                            dose,
                            confirmationSucceeded = true,
                            outcome = outcome,
                        )
                    mutableState.value =
                        VoiceConfirmationUiState(VoicePhase.CONFIRMED, dose, content)
                    val next = nextDose(GetNextDoseQuery(dose.olderAdultId))
                    if (next is AppResult.Success) {
                        content = content.copy(nextDose = next.value?.takeIf { it.id != intakeId })
                        mutableState.value = state.value.copy(confirmation = content)
                    }
                } else
                    mutableState.value =
                        state.value.copy(
                            phase =
                                if (value.status == VoiceConfirmationStatus.PROVIDER_UNAVAILABLE)
                                    VoicePhase.UNAVAILABLE
                                else VoicePhase.NOT_RECOGNIZED
                        )
            }
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        recordingJob = null
        recorder.cancel()
        if (state.value.phase in setOf(VoicePhase.RECORDING, VoicePhase.PROCESSING))
            mutableState.value = state.value.copy(phase = VoicePhase.READY)
    }

    override fun onCleared() {
        cancelRecording()
    }

    class Factory(
        private val intakeId: String,
        private val detail: GetDoseDetailQueryHandler,
        private val confirm: ConfirmDoseByVoiceCommandHandler,
        private val recorder: VoiceAudioRecorder,
        private val nextDose: GetNextDoseQueryHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            VoiceConfirmationViewModel(intakeId, detail, confirm, recorder, nextDose) as T
    }
}
