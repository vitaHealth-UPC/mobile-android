package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.MyMedicationsRepository
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import kotlinx.coroutines.CancellationException
import java.io.IOException

class RemoteMyMedicationsRepository(private val api: TreatmentApiService) : MyMedicationsRepository {
    override suspend fun list(): AppResult<List<MyMedication>> = try {
        val response = api.getMyMedications()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            AppResult.Success(body.map { item ->
                val regimen = item.treatments.firstOrNull { it.status == "ACTIVE" }
                    ?: item.treatments.firstOrNull()
                MyMedication(
                    id = item.medication.id,
                    name = item.medication.name,
                    presentation = item.medication.presentation,
                    active = item.medication.active && item.treatments.any { it.status == "ACTIVE" },
                    dose = regimen?.dose.orEmpty(),
                    instructions = regimen?.instructions.orEmpty(),
                    scheduledTimes = regimen?.scheduledTimes.orEmpty().toList(),
                )
            })
        } else {
            AppResult.Failure("", code = when(response.code()) {
                401 -> "SESSION_REQUIRED"
                403 -> "OLDER_ADULT_SESSION_REQUIRED"
                else -> "REQUEST_FAILED"
            })
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        AppResult.Failure("", exception, "NETWORK_UNAVAILABLE")
    } catch (exception: RuntimeException) {
        AppResult.Failure("", exception, "INVALID_RESPONSE")
    }
}
