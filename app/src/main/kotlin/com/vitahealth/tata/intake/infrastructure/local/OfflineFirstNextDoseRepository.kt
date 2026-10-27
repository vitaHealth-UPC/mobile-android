package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.ports.NextDoseRepository
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant

/** Returns server next-dose data and uses the cache only for connection failures. */
class OfflineFirstNextDoseRepository(
    private val remote: NextDoseRepository,
    private val local: IntakeLocalStore,
    private val now: () -> Instant = Instant::now,
) : NextDoseRepository {

    override suspend fun getNextDose(olderAdultId: String): AppResult<NextDoseReadModel?> =
        when (val result = remote.getNextDose(olderAdultId)) {
            is AppResult.Success -> {
                val value = result.value
                if (value == null) {
                    local.clearFutureDoses(olderAdultId, now())
                } else {
                    local.cacheDose(value.toDoseDetail())
                }
                result
            }

            is AppResult.Failure -> {
                if (result.code != NETWORK_UNAVAILABLE) {
                    result
                } else {
                    // No cached dose means unknown, not a server-confirmed empty schedule.
                    local.findNextDose(olderAdultId, now())?.let {
                        AppResult.Success(it.toNextDose())
                    } ?: result
                }
            }
        }
}
