package com.vitahealth.tata.intake.application.commands

/** Audio captured for one intake. Ownership remains enforced by the authenticated backend. */
class ConfirmDoseByVoiceCommand(val intakeId: String, audio: ByteArray, val contentType: String, val language: String) {
    private val recording = audio.copyOf()
    val audio: ByteArray get() = recording.copyOf()
}
