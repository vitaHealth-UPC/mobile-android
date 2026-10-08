package com.vitahealth.tata.intake.infrastructure.remote

import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.CancellationException
import java.time.Instant

class RemoteIntakeAgendaRepository(private val api: IntakeApiService) : IntakeAgendaRepository {
    override suspend fun getAgenda(olderAdultId: String, from: Instant, to: Instant): AppResult<List<DoseDetailReadModel>> = try {
        val response = api.getAgenda(olderAdultId, from.toString(), to.toString())
        val body = response.body()
        if (!response.isSuccessful || body == null) {
            AppResult.Failure("No pudimos consultar tu agenda.", code = "REQUEST_FAILED")
        } else {
            val doses = mutableListOf<DoseDetailReadModel>()
            var failure: AppResult.Failure? = null
            for (resource in body) {
                when (val result = mapIntakeDetail(resource)) {
                    is AppResult.Success -> doses.add(result.value)
                    is AppResult.Failure -> { failure = result; break }
                }
            }
            failure ?: AppResult.Success(doses.sortedWith(compareBy({ it.scheduledAt }, { it.id })))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        AppResult.Failure("No hay conexión. Inténtalo nuevamente.", cause = exception, code = "NETWORK_UNAVAILABLE")
    }
}
