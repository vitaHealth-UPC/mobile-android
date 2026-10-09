package com.vitahealth.tata.intake.application

import com.vitahealth.tata.shared.common.result.AppResult

data class VoiceRecording(val audio: ByteArray, val contentType: String)

/** Owns one temporary recording, releasing it on completion, cancellation or screen departure. */
interface VoiceAudioRecorder {
    fun start(): AppResult<Unit>
    fun finish(): AppResult<VoiceRecording>
    fun cancel()
}
