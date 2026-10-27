package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

/** Validates the entire requested adult agenda before replacing any cached range. */
class RemoteIntakeAgendaRepository(private val api: IntakeApiService) : IntakeAgendaRepository {
    override suspend fun getAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
    ): AppResult<List<DoseDetailReadModel>> =
        try {
            val response = api.getAgenda(olderAdultId, from.toString(), to.toString())
            val body = response.body()
            if (!response.isSuccessful) {
                intakeHttpFailure(response.code(), "OLDER_ADULT_NOT_FOUND")
            } else if (
                body == null ||
                    body.any { it.olderAdultId != olderAdultId } ||
                    body.map { it.id }.distinct().size != body.size
            ) {
                invalidIntakeResponse()
            } else {
                val doses = mutableListOf<DoseDetailReadModel>()
                var failure: AppResult.Failure? = null
                for (resource in body) {
                    when (val result = mapIntakeDetail(resource)) {
                        is AppResult.Success -> {
                            if (
                                result.value.scheduledAt.isBefore(from) ||
                                    !result.value.scheduledAt.isBefore(to)
                            ) {
                                failure = invalidIntakeResponse()
                                break
                            }
                            doses.add(result.value)
                        }
                        is AppResult.Failure -> {
                            failure = result
                            break
                        }
                    }
                }
                failure
                    ?: AppResult.Success(doses.sortedWith(compareBy({ it.scheduledAt }, { it.id })))
            }
        } catch (exception: Exception) {
            intakeRemoteFailure(exception)
        }
}
