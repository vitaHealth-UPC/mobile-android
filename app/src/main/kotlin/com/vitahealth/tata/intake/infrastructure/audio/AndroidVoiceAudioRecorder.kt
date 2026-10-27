package com.vitahealth.tata.intake.infrastructure.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.vitahealth.tata.intake.application.inputs.VoiceRecording
import com.vitahealth.tata.intake.application.ports.VoiceAudioRecorder
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/** Captures bounded PCM16 in memory and emits the exact WAV format required by Spring. */
class AndroidVoiceAudioRecorder(private val context: Context) : VoiceAudioRecorder {
    private val recording = AtomicBoolean(false)
    private var microphone: AudioRecord? = null
    private var reader: Thread? = null
    private var pcm: ByteArrayOutputStream? = null
    @Volatile private var recordingError: Throwable? = null

    override fun start(): AppResult<Unit> {
        cancel()
        return try {
            check(reader == null) { "Previous recording is still stopping" }
            check(context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                "Microphone permission denied"
            }
            val minBuffer = AudioRecord.getMinBufferSize(
                Pcm16WavEncoder.SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            check(minBuffer > 0) { "PCM16 recording unsupported" }
            val active = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                Pcm16WavEncoder.SAMPLE_RATE_HZ,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuffer, 4096),
            )
            microphone = active
            check(active.state == AudioRecord.STATE_INITIALIZED) { "Microphone initialization failed" }
            val output = ByteArrayOutputStream(Pcm16WavEncoder.MAX_PCM_BYTES)
            pcm = output
            recordingError = null
            active.startRecording()
            check(active.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "Microphone did not start" }
            recording.set(true)
            reader = Thread(
                {
                    val chunk = ByteArray(4096)
                    try {
                        while (recording.get() && output.size() < Pcm16WavEncoder.MAX_PCM_BYTES) {
                            val available = Pcm16WavEncoder.MAX_PCM_BYTES - output.size()
                            val received = active.read(
                                chunk, 0, minOf(chunk.size, available), AudioRecord.READ_BLOCKING,
                            )
                            when {
                                received > 0 -> {
                                    check(received % 2 == 0) { "Incomplete PCM16 sample" }
                                    output.write(chunk, 0, received)
                                }
                                received == 0 -> Thread.sleep(10)
                                recording.get() -> error("Microphone read failed: $received")
                            }
                        }
                    } catch (failure: Exception) {
                        if (recording.get()) recordingError = failure
                    }
                },
                "tata-voice-pcm-capture",
            ).apply {
                isDaemon = true
                start()
            }
            AppResult.Success(Unit)
        } catch (failure: Exception) {
            cancel()
            AppResult.Failure(
                "No pudimos iniciar el micrófono.",
                cause = failure,
                code = "MICROPHONE_UNAVAILABLE",
            )
        }
    }

    override fun finish(): AppResult<VoiceRecording> =
        try {
            check(microphone != null) { "Recording not started" }
            check(stopCapture()) { "Microphone worker did not stop" }
            recordingError?.let { throw IllegalStateException("Microphone capture failed", it) }
            val samples = pcm?.toByteArray() ?: error("Recording missing")
            AppResult.Success(Pcm16WavEncoder.encode(samples))
        } catch (failure: Exception) {
            AppResult.Failure(
                "No pudimos guardar la grabación.",
                cause = failure,
                code = "INVALID_AUDIO",
            )
        } finally {
            cancel()
        }

    override fun cancel() {
        stopCapture()
        if (reader == null) {
            pcm?.reset()
            pcm = null
            recordingError = null
        }
    }

    /** Stop unblocks blocking reads before releasing the microphone. */
    private fun stopCapture(): Boolean {
        recording.set(false)
        val active = microphone
        if (active != null) {
            if (active.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                runCatching { active.stop() }
            }
        }
        val worker = reader
        if (worker != null && worker !== Thread.currentThread()) {
            runCatching { worker.join(700) }
        }
        if (active != null) runCatching { active.release() }
        microphone = null
        if (worker != null && worker.isAlive && worker !== Thread.currentThread()) {
            runCatching { worker.interrupt() }
            runCatching { worker.join(700) }
        }
        if (worker?.isAlive == true) return false
        reader = null
        return true
    }
}
