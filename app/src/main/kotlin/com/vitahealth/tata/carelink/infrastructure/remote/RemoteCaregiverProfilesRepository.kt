package com.vitahealth.tata.carelink.infrastructure.remote

import com.google.gson.stream.MalformedJsonException
import com.vitahealth.tata.carelink.application.inputs.NewAdultProfile
import com.vitahealth.tata.carelink.application.ports.CaregiverProfilesRepository
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import com.vitahealth.tata.shared.common.result.AppResult
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.CancellationException
import retrofit2.Response

/** Maps caregiver profile endpoints into validated application contracts. */
class RemoteCaregiverProfilesRepository(private val api: CaregiverProfilesApiService) :
    CaregiverProfilesRepository {
    override suspend fun list(caregiverId: String): AppResult<List<LinkedAdult>> = request {
        val response = api.list(caregiverId)
        if (!response.isSuccessful) return@request failure(response)
        val body = requireNotNull(response.body())
        val adults = body.map { link ->
            require(link.id.isNotBlank())
            linkedAdult(link.olderAdultId, link.olderAdultName).copy(
                confirmedAt = link.confirmedAt?.let(Instant::parse),
            )
        }
        require(adults.map { it.id }.distinct().size == adults.size)
        AppResult.Success(adults)
    }

    override suspend fun register(
        caregiverId: String,
        profile: NewAdultProfile,
    ): AppResult<LinkedAdult> = request {
        val response =
            api.register(
                RegisterAdultRequest(
                    caregiverId = caregiverId,
                    fullName = profile.name.trim(),
                    birthDate = profile.birthDate.toString(),
                    emergencyContactName = profile.contactName.trim().ifBlank { null },
                    emergencyContactRelationship = profile.relationship.trim().ifBlank { null },
                    emergencyContactPhone = profile.phone.trim().ifBlank { null },
                )
            )
        if (!response.isSuccessful) return@request failure(response)
        val body = requireNotNull(response.body())
        require(body.registeredByCaregiverId == caregiverId)
        AppResult.Success(linkedAdult(body.id, body.fullName))
    }

    override suspend fun linkingCode(
        caregiverId: String,
        adult: LinkedAdult,
    ): AppResult<ProfileLinkingCode> = request {
        val response = api.code(GenerateCodeRequest(caregiverId, adult.id))
        if (!response.isSuccessful) return@request failure(response)
        val body = requireNotNull(response.body())
        require(body.caregiverId == caregiverId && body.olderAdultId == adult.id)
        require(body.id.isNotBlank() && body.status == "PENDING")
        require(body.codeUsedAt == null && body.confirmedAt == null)
        require(body.consentGranted != true && body.consentRecordedAt == null)
        val code = requireNotNull(body.linkingCode)
        val expiration = requireNotNull(body.codeExpiresAt)
        require(code.isNotBlank())
        require(Instant.parse(expiration).isAfter(Instant.now()))
        AppResult.Success(ProfileLinkingCode(adult.id, adult.name, code, expiration))
    }

    private fun linkedAdult(id: String, name: String): LinkedAdult {
        require(id.isNotBlank() && name.isNotBlank())
        return LinkedAdult(id, name)
    }

    private fun failure(response: Response<*>) =
        AppResult.Failure(
            message = "Profile request failed",
            code =
                when (response.code()) {
                    401 -> "AUTHENTICATION_REQUIRED"
                    403 -> "RESOURCE_ACCESS_DENIED"
                    400 -> "VALIDATION_ERROR"
                    else -> "REQUEST_FAILED"
                },
        )

    private suspend fun <T> request(call: suspend () -> AppResult<T>): AppResult<T> =
        try {
            call()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: MalformedJsonException) {
            AppResult.Failure("Invalid response", cause = exception, code = "INVALID_RESPONSE")
        } catch (exception: IOException) {
            AppResult.Failure(
                "Network unavailable",
                cause = exception,
                code = "NETWORK_UNAVAILABLE",
            )
        } catch (exception: RuntimeException) {
            AppResult.Failure("Invalid response", cause = exception, code = "INVALID_RESPONSE")
        }
}
