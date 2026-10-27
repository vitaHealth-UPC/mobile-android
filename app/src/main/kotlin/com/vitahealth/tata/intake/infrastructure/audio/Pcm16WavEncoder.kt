package com.vitahealth.tata.intake.infrastructure.audio

import com.vitahealth.tata.intake.application.inputs.VoiceRecording
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Builds the mono PCM16 WAV contract validated by Spring and accepted by Google Speech. */
internal object Pcm16WavEncoder {
    const val SAMPLE_RATE_HZ = 16_000
    const val MAX_SECONDS = 8
    const val MAX_PCM_BYTES = SAMPLE_RATE_HZ * 2 * MAX_SECONDS
    private const val HEADER_BYTES = 44

    fun encode(pcm: ByteArray): VoiceRecording {
        require(pcm.isNotEmpty() && pcm.size <= MAX_PCM_BYTES && pcm.size % 2 == 0) {
            "PCM recording must contain whole 16-bit samples within eight seconds"
        }
        val wav = ByteBuffer.allocate(HEADER_BYTES + pcm.size).order(ByteOrder.LITTLE_ENDIAN)
        wav.put("RIFF".toByteArray(Charsets.US_ASCII))
        wav.putInt(36 + pcm.size)
        wav.put("WAVE".toByteArray(Charsets.US_ASCII))
        wav.put("fmt ".toByteArray(Charsets.US_ASCII))
        wav.putInt(16)
        wav.putShort(1.toShort()) // PCM
        wav.putShort(1.toShort()) // mono
        wav.putInt(SAMPLE_RATE_HZ)
        wav.putInt(SAMPLE_RATE_HZ * 2) // bytes per second
        wav.putShort(2.toShort()) // block alignment
        wav.putShort(16.toShort()) // sample bits
        wav.put("data".toByteArray(Charsets.US_ASCII))
        wav.putInt(pcm.size)
        wav.put(pcm)
        return VoiceRecording(wav.array(), "audio/wav")
    }
}
