package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.NextDoseRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.sync.SyncScheduler
import java.time.Instant

class OfflineFirstNextDoseRepository(
    private val remote: NextDoseRepository,
    private val local: IntakeLocalStore,
    private val now: () -> Instant = Instant::now,
) : NextDoseRepository {

    override suspend fun getNextDose(
        olderAdultId: String,
    ): AppResult<NextDoseReadModel?> =
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
                    AppResult.Success(
                        local.findNextDose(olderAdultId, now())?.toNextDose(),
                    )
                }
            }
        }
}

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

class OfflineFirstDoseDetailRepository(
    private val remote: DoseDetailRepository,
    private val local: IntakeLocalStore,
) : DoseDetailRepository {

    override suspend fun getDoseDetail(
        intakeId: String,
    ): AppResult<DoseDetailReadModel> =
        when (val result = remote.getDoseDetail(intakeId)) {
            is AppResult.Success -> {
                local.cacheDose(result.value)
                result
            }

            is AppResult.Failure -> {
                if (result.code != NETWORK_UNAVAILABLE) {
                    result
                } else {
                    local.findDose(intakeId)?.let(AppResult::Success)
                        ?: result
                }
            }
        }
}

class OfflineFirstDoseConfirmationRepository(
    private val remote: DoseConfirmationRepository,
    private val local: IntakeLocalStore,
    private val syncScheduler: SyncScheduler,
) : DoseConfirmationRepository {

    override suspend fun confirm(
        command: ConfirmDoseCommand,
    ): AppResult<DoseDetailReadModel> =
        when (val result = remote.confirm(command)) {
            is AppResult.Success -> {
                local.cacheDose(result.value)
                local.removePendingConfirmation(command.intakeId)
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
                        message = "La confirmación quedó pendiente y se sincronizará cuando vuelva la conexión.",
                        code = PENDING_SYNC,
                    )
                }
            }
        }
}

private fun NextDoseReadModel.toDoseDetail() = DoseDetailReadModel(
    id = id,
    treatmentId = treatmentId,
    medicationId = medicationId,
    olderAdultId = olderAdultId,
    medicationName = medicationName,
    dose = dose,
    instructions = instructions,
    scheduledAt = scheduledAt,
    status = status,
)

private fun DoseDetailReadModel.toNextDose() = NextDoseReadModel(
    id = id,
    treatmentId = treatmentId,
    medicationId = medicationId,
    olderAdultId = olderAdultId,
    medicationName = medicationName,
    dose = dose,
    instructions = instructions,
    scheduledAt = scheduledAt,
    status = status,
)

private const val NETWORK_UNAVAILABLE = "NETWORK_UNAVAILABLE"
private const val PENDING_SYNC = "PENDING_SYNC"
