package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.VoiceConfirmationRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseByVoiceCommand
import com.vitahealth.tata.intake.application.readmodels.*
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Sends audio to the voice endpoint; it never substitutes an ordinary touch confirmation. */
class RemoteVoiceConfirmationRepository(private val api: VoiceConfirmationApiService) : VoiceConfirmationRepository {
    override suspend fun confirm(command: ConfirmDoseByVoiceCommand): AppResult<VoiceConfirmationReadModel> = try {
        val audio = MultipartBody.Part.createFormData("audio", "confirmation.m4a", command.audio.toRequestBody(command.contentType.toMediaType()))
        val response = api.confirm(command.intakeId, audio, command.language)
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            val title = runCatching { com.google.gson.JsonParser.parseString(response.errorBody()?.string().orEmpty()).asJsonObject.get("title")?.asString }.getOrNull()
            val code = when(response.code()) { 404 -> "INTAKE_NOT_FOUND"; 409 -> if (title == "VOICE_CONFIRMATION_DISABLED") title else "INTAKE_NOT_CONFIRMABLE"; 403 -> "ACCESS_DENIED"; 401 -> "SESSION_REQUIRED"; else -> "REQUEST_FAILED" }
            AppResult.Failure("No pudimos confirmar por voz. Puedes volver y confirmar por toque.", code = code)
        } else {
            val status = VoiceConfirmationStatus.entries.firstOrNull { it.name == body.status }
            if (status == null) invalidResult()
            else if (status == VoiceConfirmationStatus.CONFIRMED || status == VoiceConfirmationStatus.ALREADY_CONFIRMED) {
                val intake = body.intake
                if (intake == null || intake.id != command.intakeId || intake.confirmedAt == null) invalidResult()
                else when (val mapped = mapIntakeDetail(intake)) {
                    is AppResult.Failure -> mapped
                    is AppResult.Success -> if (mapped.value.confirmedAt == null || mapped.value.status !in setOf(DoseStatus.CONFIRMED, DoseStatus.LATE)) invalidResult()
                        else AppResult.Success(VoiceConfirmationReadModel(status, mapped.value.copy(alreadyConfirmed = status == VoiceConfirmationStatus.ALREADY_CONFIRMED)))
                }
            } else if (body.intake != null) invalidResult()
            else AppResult.Success(VoiceConfirmationReadModel(status))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure("No hay conexión. Puedes volver y confirmar por toque.", cause = exception, code = "NETWORK_UNAVAILABLE")
    }

    private fun invalidResult() = AppResult.Failure("La respuesta de voz no es válida.", code = "INVALID_VOICE_RESULT")
}
