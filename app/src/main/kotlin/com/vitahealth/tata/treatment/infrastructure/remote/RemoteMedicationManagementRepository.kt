package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MedicationReactivationPort
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.repositories.MedicationManagementRepository
import retrofit2.Response

/** Lists, updates and deactivates registered medications. */
class RemoteMedicationManagementRepository(private val api: TreatmentApiService) :
    MedicationManagementRepository, MedicationReactivationPort {
    override suspend fun reactivate(
        caregiverId: String,
        medicationId: String,
    ): AppResult<Medication> =
        medicationRequest(medicationId, validateReceipt = { require(it.active == true) }) {
            api.reactivateMedication(medicationId, caregiverId)
        }

    override suspend fun listMedications(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<List<Medication>> =
        try {
            val response =
                api.listMedications(olderAdultId = olderAdultId, caregiverId = caregiverId)
            if (response.isSuccessful) {
                val body = requireNotNull(response.body()) { "Missing treatment resource" }
                require(body.all { it.olderAdultId == olderAdultId })
                require(body.map { it.id }.distinct().size == body.size)
                AppResult.Success(body.map { it.toDomain() })
            } else {
                AppResult.Failure(
                    message = treatmentMessage(response.code()),
                    code = treatmentCode(response.code()),
                )
            }
        } catch (exception: Exception) {
            requestFailure(exception)
        }

    override suspend fun updateMedication(
        caregiverId: String,
        medicationId: String,
        name: String,
        presentation: String,
    ): AppResult<Medication> =
        medicationRequest(
            medicationId,
            validateReceipt = {
                require(it.name == name.trim() && it.presentation == presentation.trim())
            },
        ) {
            api.updateMedication(
                medicationId = medicationId,
                request =
                    UpdateMedicationRequest(
                        caregiverId = caregiverId,
                        name = name,
                        presentation = presentation,
                    ),
            )
        }

    override suspend fun deactivateMedication(
        caregiverId: String,
        medicationId: String,
    ): AppResult<Medication> =
        medicationRequest(medicationId, validateReceipt = { require(it.active == false) }) {
            api.deactivateMedication(medicationId, caregiverId)
        }

    private suspend fun medicationRequest(
        expectedMedicationId: String,
        validateReceipt: (MedicationResponse) -> Unit = {},
        request: suspend () -> Response<MedicationResponse>,
    ): AppResult<Medication> =
        try {
            val response = request()
            if (response.isSuccessful) {
                val body = requireNotNull(response.body()) { "Missing treatment resource" }
                require(body.id == expectedMedicationId)
                validateReceipt(body)
                AppResult.Success(body.toDomain())
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
