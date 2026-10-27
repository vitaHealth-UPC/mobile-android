package com.vitahealth.tata.intake.presentation.voice

import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommandHandler
import com.vitahealth.tata.intake.application.handlers.*
import com.vitahealth.tata.intake.application.inputs.VoiceRecording
import com.vitahealth.tata.intake.application.ports.DoseDetailRepository
import com.vitahealth.tata.intake.application.ports.NextDoseRepository
import com.vitahealth.tata.intake.application.ports.VoiceAudioRecorder
import com.vitahealth.tata.intake.application.ports.VoiceConfirmationRepository
import com.vitahealth.tata.intake.application.queries.GetDoseDetailQueryHandler
import com.vitahealth.tata.intake.application.queries.GetNextDoseQueryHandler
import com.vitahealth.tata.intake.application.readmodels.*
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoiceConfirmationViewModel
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoicePhase
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class VoiceConfirmationViewModelTest {
    private val dose =
        DoseDetailReadModel(
            "intake-1",
            "t",
            "m",
            "adult",
            "Losartán 50 mg",
            "1 comprimido",
            "",
            Instant.parse("2026-10-08T13:00:00Z"),
            DoseStatus.PENDING,
        )

    private class Recorder : VoiceAudioRecorder {
        var starts = 0
        var cancellations = 0

        override fun start(): AppResult<Unit> {
            starts++
            return AppResult.Success(Unit)
        }

        override fun finish() = AppResult.Success(VoiceRecording(byteArrayOf(1, 2, 3), "audio/mp4"))

        override fun cancel() {
            cancellations++
        }
    }

    private fun model(
        recorder: Recorder,
        answer: VoiceConfirmationReadModel,
        gate: kotlinx.coroutines.CompletableDeferred<Unit>? = null,
        onProviderFinished: () -> Unit = {},
    ): VoiceConfirmationViewModel {
        val details =
            object : DoseDetailRepository {
                override suspend fun getDoseDetail(intakeId: String) = AppResult.Success(dose)
            }
        val voices =
            object : VoiceConfirmationRepository {
                override suspend fun confirm(
                    command: ConfirmDoseByVoiceCommand
                ): AppResult<VoiceConfirmationReadModel> {
                    try {
                        gate?.await()
                        return AppResult.Success(answer)
                    } finally {
                        onProviderFinished()
                    }
                }
            }
        val next =
            object : NextDoseRepository {
                override suspend fun getNextDose(
                    olderAdultId: String
                ): AppResult<NextDoseReadModel?> = AppResult.Failure("offline")
            }
        return VoiceConfirmationViewModel(
            dose.id,
            GetDoseDetailQueryHandler(details),
            ConfirmDoseByVoiceCommandHandler(voices),
            recorder,
            GetNextDoseQueryHandler(next),
            Dispatchers.Unconfined,
        )
    }

    @Test
    fun permissionDenialNeverStartsRecording() {
        val recorder = Recorder()
        val model =
            model(recorder, VoiceConfirmationReadModel(VoiceConfirmationStatus.NOT_RECOGNIZED))
        model.permissionDenied()
        assertEquals(VoicePhase.PERMISSION_DENIED, model.state.value.phase)
        assertEquals(0, recorder.starts)
    }

    @Test
    fun repeatedStartDoesNotOpenMultipleRecordingsAndDepartureReleasesAudio() {
        val recorder = Recorder()
        val model =
            model(recorder, VoiceConfirmationReadModel(VoiceConfirmationStatus.NOT_RECOGNIZED))
        model.start("es-419")
        model.start("es-419")
        assertEquals(1, recorder.starts)
        assertEquals(VoicePhase.RECORDING, model.state.value.phase)
        model.cancelRecording()
        assertEquals(VoicePhase.READY, model.state.value.phase)
        assertEquals(1, recorder.cancellations)
    }

    @Test
    fun unrecognizedPhraseDoesNotShowSuccess() = runBlocking {
        val recorder = Recorder()
        val model =
            model(recorder, VoiceConfirmationReadModel(VoiceConfirmationStatus.NOT_RECOGNIZED))
        model.start("es-419")
        model.finish("es-419")
        assertEquals(VoicePhase.NOT_RECOGNIZED, model.state.value.phase)
        assertNull(model.state.value.confirmation)
        model.cancelRecording()
    }

    @Test
    fun nextDoseFailurePreservesThePersistedConfirmationAndReplayOutcome() = runBlocking {
        val recorder = Recorder()
        val saved =
            dose.copy(
                status = DoseStatus.CONFIRMED,
                confirmedAt = Instant.parse("2026-10-08T13:01:00Z"),
                alreadyConfirmed = true,
            )
        val model =
            model(
                recorder,
                VoiceConfirmationReadModel(VoiceConfirmationStatus.ALREADY_CONFIRMED, saved),
            )
        model.start("es-419")
        model.finish("es-419")
        assertEquals(VoicePhase.CONFIRMED, model.state.value.phase)
        assertEquals(
            ConfirmationOutcome.ALREADY_CONFIRMED,
            model.state.value.confirmation!!.outcome,
        )
        assertEquals(saved.confirmedAt, model.state.value.confirmation!!.dose.confirmedAt)
        model.cancelRecording()
    }

    @Test
    fun cancelledAttemptCannotPublishDelayedProviderSuccess() = runBlocking {
        val recorder = Recorder()
        val gate = kotlinx.coroutines.CompletableDeferred<Unit>()
        var providerFinished = false
        val saved =
            dose.copy(
                status = DoseStatus.CONFIRMED,
                confirmedAt = Instant.parse("2026-10-08T13:01:00Z"),
            )
        val model =
            model(
                recorder,
                VoiceConfirmationReadModel(VoiceConfirmationStatus.CONFIRMED, saved),
                gate,
                onProviderFinished = { providerFinished = true },
            )
        model.start("es-419")
        val operation =
            kotlinx.coroutines.CoroutineScope(Dispatchers.Unconfined).launch {
                model.finish("es-419")
            }
        assertEquals(VoicePhase.PROCESSING, model.state.value.phase)
        model.cancelRecording()
        assertTrue(providerFinished)
        assertTrue(operation.isCancelled)
        gate.complete(Unit)
        operation.join()
        assertEquals(VoicePhase.READY, model.state.value.phase)
        assertNull(model.state.value.confirmation)
    }
}
