package com.vitahealth.tata

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.intake.infrastructure.audio.AndroidVoiceAudioRecorder
import com.vitahealth.tata.shared.common.result.AppResult
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises Android's recorder and temporary-file lifecycle without contacting a speech provider. */
@RunWith(AndroidJUnit4::class)
class AndroidVoiceAudioRecorderTest {

    @Test fun capturesBackendCompatiblePcm16WavWithoutLeavingARecordingFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.RECORD_AUDIO)
        val before = context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("intake-voice-") }.map { it.name }.toSet()
        val recorder = AndroidVoiceAudioRecorder(context)
        try {
            assertTrue(recorder.start() is AppResult.Success)
            android.os.SystemClock.sleep(1500)
            val result = recorder.finish()
            assertTrue("Recorder must return captured audio: $result", result is AppResult.Success)
            val audio = (result as AppResult.Success).value
            assertEquals("audio/wav", audio.contentType)
            assertTrue(audio.audio.size > 44)
            assertEquals("RIFF", String(audio.audio, 0, 4, Charsets.US_ASCII))
            assertEquals("WAVE", String(audio.audio, 8, 4, Charsets.US_ASCII))
            assertEquals("fmt ", String(audio.audio, 12, 4, Charsets.US_ASCII))
            assertEquals("data", String(audio.audio, 36, 4, Charsets.US_ASCII))
            val header = java.nio.ByteBuffer.wrap(audio.audio).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            assertEquals(audio.audio.size - 8, header.getInt(4))
            assertEquals(1, header.getShort(20).toInt())
            assertEquals(1, header.getShort(22).toInt())
            assertEquals(16000, header.getInt(24))
            assertEquals(32000, header.getInt(28))
            assertEquals(2, header.getShort(32).toInt())
            assertEquals(16, header.getShort(34).toInt())
            assertEquals(audio.audio.size - 44, header.getInt(40))
            assertTrue("The recorder must not exceed the eight-second cap", audio.audio.size <= 44 + 16000 * 2 * 8)
        } finally { recorder.cancel() }
        val after = context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("intake-voice-") }.map { it.name }.toSet()
        assertEquals(before, after)
    }
}
