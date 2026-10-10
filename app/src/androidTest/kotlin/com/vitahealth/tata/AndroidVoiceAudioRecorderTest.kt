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

    @Test fun capturesMp4AudioAndDeletesThePrivateTemporaryFile() {
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
            assertEquals("audio/mp4", audio.contentType)
            assertTrue(audio.audio.size > 8)
            assertEquals("ftyp", String(audio.audio, 4, 4, Charsets.US_ASCII))
        } finally { recorder.cancel() }
        val after = context.cacheDir.listFiles().orEmpty().filter { it.name.startsWith("intake-voice-") }.map { it.name }.toSet()
        assertEquals(before, after)
    }
}
