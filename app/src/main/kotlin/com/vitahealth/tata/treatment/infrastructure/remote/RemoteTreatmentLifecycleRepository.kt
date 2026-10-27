package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository
import java.time.LocalTime
import retrofit2.Response

/** Configures regimens and changes treatment lifecycle state. */
class RemoteTreatmentLifecycleRepository(private val api: TreatmentApiService) :
    TreatmentLifecycleRepository {
    override suspend fun configureTreatment(
        caregiverId: String,
        treatmentId: String,
        medicationId: String,
        dose: String,
        frequency: String,
        scheduledTimes: List<String>,
        instructions: String,
        reminderLeadMinutes: Int,
        followUpDelayMinutes: Int?,
    ): AppResult<Treatment> =
        treatmentRequest(
            treatmentId,
            medicationId,
            validateReceipt = { body ->
                require(body.dose == dose.trim() && body.frequency == frequency.trim())
                require(body.instructions == instructions.trim())
                require(body.reminderLeadMinutes == reminderLeadMinutes)
                require(body.followUpDelayMinutes == followUpDelayMinutes)
                val receivedTimes = requireNotNull(body.scheduledTimes).map(LocalTime::parse)
                val requestedTimes =
                    scheduledTimes.map { LocalTime.parse(it.trim()) }.distinct().sorted()
                require(receivedTimes.distinct().size == receivedTimes.size)
                require(receivedTimes.sorted() == requestedTimes)
            },
        ) {
            api.configureTreatment(
                treatmentId = treatmentId,
                request =
                    ConfigureTreatmentRequest(
                        caregiverId = caregiverId,
                        medicationId = medicationId,
                        dose = dose,
                        frequency = frequency,
                        scheduledTimes = scheduledTimes,
                        instructions = instructions,
                        reminderLeadMinutes = reminderLeadMinutes,
                        followUpDelayMinutes = followUpDelayMinutes,
                    ),
            )
        }

    override suspend fun activateTreatment(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<Treatment> =
        treatmentRequest(treatmentId, expectedStatus = TreatmentStatus.ACTIVE) {
            api.activateTreatment(treatmentId, caregiverId)
        }

    override suspend fun pauseTreatment(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<Treatment> =
        treatmentRequest(treatmentId, expectedStatus = TreatmentStatus.PAUSED) {
            api.pauseTreatment(treatmentId, caregiverId)
        }

    override suspend fun resumeTreatment(
        caregiverId: String,
        treatmentId: String,
    ): AppResult<Treatment> =
        treatmentRequest(treatmentId, expectedStatus = TreatmentStatus.ACTIVE) {
            api.resumeTreatment(treatmentId, caregiverId)
        }

    private suspend fun treatmentRequest(
        expectedTreatmentId: String,
        expectedMedicationId: String? = null,
        expectedStatus: TreatmentStatus? = null,
        validateReceipt: (TreatmentResponse) -> Unit = {},
        request: suspend () -> Response<TreatmentResponse>,
    ): AppResult<Treatment> {
        return try {
            val response = request()
            if (response.isSuccessful) {
                val body = requireNotNull(response.body()) { "Missing treatment resource" }
                require(body.id == expectedTreatmentId)
                require(expectedMedicationId == null || body.medicationId == expectedMedicationId)
                val status =
                    runCatching { TreatmentStatus.valueOf(body.status) }.getOrNull()
                        ?: return AppResult.Failure(
                            message =
                                "El servicio devolvió un estado de tratamiento no reconocido.",
                            code = "INVALID_TREATMENT_STATE",
                        )
                require(expectedStatus == null || status == expectedStatus)
                validateReceipt(body)
                AppResult.Success(body.toDomain(status))
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            requestFailure(exception)
        }
    }
}
