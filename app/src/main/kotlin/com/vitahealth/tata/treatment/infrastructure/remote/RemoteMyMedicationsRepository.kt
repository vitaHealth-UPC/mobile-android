package com.vitahealth.tata.treatment.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.ports.MyMedicationsRepository
import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import java.io.IOException
import kotlinx.coroutines.CancellationException

/** Retrieves the authenticated adult catalog and rejects malformed successful responses. */
class RemoteMyMedicationsRepository(private val api: TreatmentApiService) :
    MyMedicationsRepository {
    override suspend fun list(): AppResult<List<MyMedication>> =
        try {
            val response = api.getMyMedications()
            val body = response.body()
            if (response.isSuccessful) {
                val resources = requireNotNull(body)
                require(resources.map { it.medication.id }.distinct().size == resources.size)
                AppResult.Success(resources.map(MyMedicationResponse::toReadModel))
            } else {
                AppResult.Failure(
                    "",
                    code =
                        when (response.code()) {
                            401 -> "SESSION_REQUIRED"
                            403 -> "OLDER_ADULT_SESSION_REQUIRED"
                            else -> "REQUEST_FAILED"
                        },
                )
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("", exception, "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure("", exception, "NETWORK_UNAVAILABLE")
        } catch (exception: RuntimeException) {
            AppResult.Failure("", exception, "INVALID_RESPONSE")
        }
}
