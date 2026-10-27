package com.vitahealth.tata.intake.application.inputs

/** Recorded audio supplied to the voice confirmation use case. */
data class VoiceRecording(val audio: ByteArray, val contentType: String)
