package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.ports.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

/** Refreshes the requested agenda range or reads its cached copy when offline. */
class OfflineFirstIntakeAgendaRepository(
    private val remote: IntakeAgendaRepository,
    private val local: IntakeLocalStore,
) : IntakeAgendaRepository {

    override suspend fun getAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
    ): AppResult<List<DoseDetailReadModel>> =
        when (val result = remote.getAgenda(olderAdultId, from, to)) {
            is AppResult.Success -> {
                local.replaceAgenda(
                    olderAdultId = olderAdultId,
                    from = from,
                    to = to,
                    doses = result.value,
                )
                result
            }

            is AppResult.Failure -> {
                if (result.code != NETWORK_UNAVAILABLE) {
                    result
                } else {
                    AppResult.Success(local.findAgenda(olderAdultId, from, to))
                }
            }
        }
}
