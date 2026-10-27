package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.ports.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.intake.application.ports.SyncScheduler

/** Persists retryable offline confirmations without fabricating an acknowledged outcome. */
class OfflineFirstDoseConfirmationRepository(
    private val remote: DoseConfirmationRepository,
    private val local: IntakeLocalStore,
    private val syncScheduler: SyncScheduler,
) : DoseConfirmationRepository {

    override suspend fun confirm(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel> =
        when (val result = remote.confirm(command)) {
            is AppResult.Success -> {
                local.acknowledgeConfirmation(result.value)
                result
            }

            is AppResult.Failure -> {
                if (result.code != NETWORK_UNAVAILABLE) {
                    result
                } else if (local.findDose(command.intakeId) == null) {
                    result
                } else {
                    local.enqueueConfirmation(command)
                    syncScheduler.schedule()

                    AppResult.Failure(
                        message =
                            "La confirmación quedó pendiente y se sincronizará cuando vuelva la conexión.",
                        code = PENDING_SYNC,
                    )
                }
            }
        }
}
