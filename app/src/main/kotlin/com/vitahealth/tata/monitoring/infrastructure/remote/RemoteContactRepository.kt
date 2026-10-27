package com.vitahealth.tata.monitoring.infrastructure.remote

import com.vitahealth.tata.monitoring.application.AlertFailureCodes
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannel
import com.vitahealth.tata.monitoring.domain.repositories.ContactRepository
import com.vitahealth.tata.shared.common.result.AppResult

/**
 * Contact channel (`GET /older-adults/{id}/contact-channel`) and name (`GET /older-adults/{id}`) of
 * the older adult.
 */
class RemoteContactRepository(private val api: FamilyMonitoringApiService) : ContactRepository {

    override suspend fun contactChannel(
        caregiverId: String,
        olderAdultId: String,
    ): AppResult<ContactChannel> = guardedFollowUp {
        val response = api.contact(olderAdultId, caregiverId)
        val body = response.body()
        followUpHttpFailure(response)
            ?: if (body == null) {
                followUpFailure(AlertFailureCodes.INVALID_RESPONSE)
            } else {
                AppResult.Success(body.toDomain())
            }
    }

    override suspend fun olderAdultFullName(olderAdultId: String): AppResult<String> =
        guardedFollowUp {
            val response = api.profile(olderAdultId)
            val body = response.body()
            val name = body?.fullName
            followUpHttpFailure(response)
                ?: if (name.isNullOrBlank() || body?.id != olderAdultId) {
                    followUpFailure(AlertFailureCodes.INVALID_RESPONSE)
                } else {
                    AppResult.Success(name)
                }
        }
}
