package com.vitahealth.tata.intake.presentation.voice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommandHandler
import com.vitahealth.tata.intake.application.ports.VoiceAudioRecorder
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQuery
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQueryHandler
import com.vitahealth.tata.intake.application.queries.GetNextDoseQuery
import com.vitahealth.tata.intake.application.queries.GetNextDoseQueryHandler
import com.vitahealth.tata.intake.application.readmodels.VoiceConfirmationStatus
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
import com.vitahealth.tata.shared.common.result.AppResult
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Coordinates temporary audio and ignores results belonging to a cancelled voice attempt. */
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
    private var loadJob: Job? = null
    private var processingJob: Deferred<Unit>? = null
    private var generation = 0L

    init {
        load()
    }

    fun load() {
        if (
            state.value.phase in
                setOf(VoicePhase.RECORDING, VoicePhase.PROCESSING, VoicePhase.CONFIRMED)
        )
            return
        loadJob?.cancel()
        val expectedGeneration = generation
        loadJob =
            viewModelScope.launch(context) {
                val result = detail(GetDoseDetailQuery(intakeId))
                if (generation != expectedGeneration) return@launch
                mutableState.value =
                    when (result) {
                        is AppResult.Success ->
                            VoiceConfirmationUiState(VoicePhase.READY, result.value)
                        is AppResult.Failure -> VoiceConfirmationUiState(VoicePhase.UNAVAILABLE)
                    }
            }
    }

    fun permissionDenied() {
        cancelRecording()
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
        loadJob?.cancel()
        generation++
        when (recorder.start()) {
            is AppResult.Failure ->
                mutableState.value = current.copy(phase = VoicePhase.UNAVAILABLE)
            is AppResult.Success -> {
                mutableState.value = current.copy(phase = VoicePhase.RECORDING)
                recordingJob =
                    viewModelScope.launch(context) {
                        delay(8000)
                        recordingJob = null
                        finish(language)
                    }
            }
        }
    }

    suspend fun finish(language: String) {
        if (state.value.phase != VoicePhase.RECORDING || processingJob?.isActive == true) return
        val operation =
            viewModelScope.async(context, start = CoroutineStart.LAZY) {
                processRecording(language)
            }
        processingJob = operation
        try {
            operation.start()
            operation.await()
        } finally {
            if (processingJob === operation) processingJob = null
        }
    }

    /** Keeps the provider request and optional next-dose query under one cancellable operation. */
    private suspend fun processRecording(language: String) {
        if (state.value.phase != VoicePhase.RECORDING) return
        recordingJob?.cancel()
        recordingJob = null
        val expectedGeneration = generation
        val audio = recorder.finish()
        if (audio is AppResult.Failure) {
            mutableState.value = state.value.copy(phase = VoicePhase.UNAVAILABLE)
            return
        }
        audio as AppResult.Success
        mutableState.value = state.value.copy(phase = VoicePhase.PROCESSING)
        val result =
            confirm(
                ConfirmDoseByVoiceCommand(
                    intakeId,
                    audio.value.audio,
                    audio.value.contentType,
                    language,
                )
            )
        if (generation != expectedGeneration) return
        when (result) {
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
                    if (generation != expectedGeneration) return
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
        generation++
        loadJob?.cancel()
        processingJob?.cancel()
        processingJob = null
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T = run {
            require(modelClass.isAssignableFrom(VoiceConfirmationViewModel::class.java))
            VoiceConfirmationViewModel(intakeId, detail, confirm, recorder, nextDose) as T
        }
    }
}
