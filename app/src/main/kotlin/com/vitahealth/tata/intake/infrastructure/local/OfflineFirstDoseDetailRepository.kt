package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.ports.DoseDetailRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult

/** Reads a dose detail without converting authorization failures into cached success. */
class OfflineFirstDoseDetailRepository(
    private val remote: DoseDetailRepository,
    private val local: IntakeLocalStore,
) : DoseDetailRepository {

    override suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel> =
        when (val result = remote.getDoseDetail(intakeId)) {
            is AppResult.Success -> {
                local.cacheDose(result.value)
                result
            }

            is AppResult.Failure -> {
                if (result.code != NETWORK_UNAVAILABLE) {
                    result
                } else {
                    local.findDose(intakeId)?.let { cached -> AppResult.Success(cached) } ?: result
                }
            }
        }
}
