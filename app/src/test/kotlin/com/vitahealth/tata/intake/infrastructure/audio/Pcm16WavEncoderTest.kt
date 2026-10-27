package com.vitahealth.tata.intake.infrastructure.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Pcm16WavEncoderTest {
    @Test
    fun writesValidMonoPcm16WaveWithExactRiffLengths() {
        val samples = byteArrayOf(0, 0, 1, 0, -1, 127, 0, -128)
        val result = Pcm16WavEncoder.encode(samples)
        assertEquals("audio/wav", result.contentType)
        val wav = result.audio
        assertEquals(44 + samples.size, wav.size)
        assertEquals("RIFF", String(wav, 0, 4, Charsets.US_ASCII))
        assertEquals("WAVE", String(wav, 8, 4, Charsets.US_ASCII))
        assertEquals("fmt ", String(wav, 12, 4, Charsets.US_ASCII))
        assertEquals("data", String(wav, 36, 4, Charsets.US_ASCII))
        val header = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(wav.size - 8, header.getInt(4))
        assertEquals(16, header.getInt(16))
        assertEquals(1, header.getShort(20).toInt())
        assertEquals(1, header.getShort(22).toInt())
        assertEquals(16_000, header.getInt(24))
        assertEquals(32_000, header.getInt(28))
        assertEquals(2, header.getShort(32).toInt())
        assertEquals(16, header.getShort(34).toInt())
        assertEquals(samples.size, header.getInt(40))
        assertArrayEquals(samples, wav.copyOfRange(44, wav.size))
    }

    @Test
    fun rejectsInvalidOrOverlongPcmBeforeUploading() {
        assertThrows(IllegalArgumentException::class.java) {
            Pcm16WavEncoder.encode(byteArrayOf())
        }
        assertThrows(IllegalArgumentException::class.java) {
            Pcm16WavEncoder.encode(byteArrayOf(1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            Pcm16WavEncoder.encode(ByteArray(Pcm16WavEncoder.MAX_PCM_BYTES + 2))
        }
        val max = Pcm16WavEncoder.encode(ByteArray(Pcm16WavEncoder.MAX_PCM_BYTES))
        assertEquals(44 + Pcm16WavEncoder.MAX_PCM_BYTES, max.audio.size)
    }
}
