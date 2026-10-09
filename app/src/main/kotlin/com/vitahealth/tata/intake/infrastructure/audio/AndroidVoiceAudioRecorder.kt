package com.vitahealth.tata.intake.infrastructure.audio

import android.content.Context
import android.media.MediaRecorder
import com.vitahealth.tata.intake.application.VoiceAudioRecorder
import com.vitahealth.tata.intake.application.VoiceRecording
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.File

/** AAC in MP4 is uploaded as audio/mp4; recordings stay in private cache and are always deleted. */
class AndroidVoiceAudioRecorder(private val context: Context) : VoiceAudioRecorder {
    private var recorder: MediaRecorder? = null
    private var file: File? = null

    override fun start(): AppResult<Unit> {
        cancel()
        return try {
            val recording = File.createTempFile("intake-voice-", ".m4a", context.cacheDir)
            file = recording
            val active = MediaRecorder(context)
            recorder = active
            active.setAudioSource(MediaRecorder.AudioSource.MIC)
            active.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            active.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            active.setAudioSamplingRate(16000)
            active.setAudioEncodingBitRate(64000)
            active.setMaxFileSize(5L * 1024 * 1024)
            active.setOutputFile(recording.absolutePath)
            active.prepare()
            active.start()
            AppResult.Success(Unit)
        } catch (exception: Exception) {
            cancel()
            AppResult.Failure(
                "No pudimos iniciar el micrófono.",
                cause = exception,
                code = "MICROPHONE_UNAVAILABLE",
            )
        }
    }

    override fun finish(): AppResult<VoiceRecording> =
        try {
            val active = recorder ?: error("Recording not started")
            active.stop()
            active.release()
            recorder = null
            val bytes = file?.readBytes() ?: error("Recording missing")
            if (bytes.isEmpty() || bytes.size > 5 * 1024 * 1024) error("Invalid recording size")
            AppResult.Success(VoiceRecording(bytes, "audio/mp4"))
        } catch (exception: Exception) {
            AppResult.Failure(
                "No pudimos guardar la grabación.",
                cause = exception,
                code = "INVALID_AUDIO",
            )
        } finally {
            cancel()
        }

    override fun cancel() {
        recorder?.let { active ->
            runCatching { active.reset() }
            runCatching { active.release() }
        }
        recorder = null
        file?.delete()
        file = null
    }
}
