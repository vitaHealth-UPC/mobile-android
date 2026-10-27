package com.vitahealth.tata.intake.application.ports

import com.vitahealth.tata.intake.application.inputs.VoiceRecording
import com.vitahealth.tata.shared.common.result.AppResult

/** Owns one temporary recording, releasing it on completion, cancellation or screen departure. */
interface VoiceAudioRecorder {
    fun start(): AppResult<Unit>

    fun finish(): AppResult<VoiceRecording>

    fun cancel()
}
