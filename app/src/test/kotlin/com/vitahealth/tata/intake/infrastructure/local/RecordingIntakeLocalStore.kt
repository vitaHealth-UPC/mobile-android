package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import java.time.Instant

/** In-memory account partition used to observe repository and replay coordination. */
internal class RecordingIntakeLocalStore : IntakeLocalStore {
    val cached = linkedMapOf<String, DoseDetailReadModel>()
    val pending = linkedMapOf<String, ConfirmDoseCommand>()
    val rejected = linkedMapOf<String, String>()
    var acknowledgements = 0

    override suspend fun cacheDose(dose: DoseDetailReadModel) {
        cached[dose.id] = dose
    }

    override suspend fun acknowledgeConfirmation(dose: DoseDetailReadModel) {
        acknowledgements++
        cached[dose.id] = dose
        pending.remove(dose.id)
    }

    override suspend fun replaceAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
        doses: List<DoseDetailReadModel>,
    ) {
        cached.entries.removeIf { (_, value) ->
            value.olderAdultId == olderAdultId &&
                value.scheduledAt >= from &&
                value.scheduledAt < to
        }
        doses.forEach { cached[it.id] = it }
    }

    override suspend fun clearFutureDoses(olderAdultId: String, from: Instant) {
        cached.entries.removeIf { (_, value) ->
            value.olderAdultId == olderAdultId && value.scheduledAt >= from
        }
    }

    override suspend fun findDose(intakeId: String) = cached[intakeId]

    override suspend fun findNextDose(olderAdultId: String, from: Instant) =
        cached.values
            .filter {
                it.olderAdultId == olderAdultId &&
                    it.scheduledAt >= from &&
                    it.status == DoseStatus.PENDING
            }
            .minByOrNull { it.scheduledAt }

    override suspend fun findAgenda(olderAdultId: String, from: Instant, to: Instant) =
        cached.values.filter {
            it.olderAdultId == olderAdultId && it.scheduledAt >= from && it.scheduledAt < to
        }

    override suspend fun enqueueConfirmation(command: ConfirmDoseCommand) {
        pending.putIfAbsent(command.intakeId, command)
    }

    override suspend fun pendingConfirmations() =
        pending.filterKeys { it !in rejected }.values.toList()

    override suspend fun removePendingConfirmation(intakeId: String) {
        pending.remove(intakeId)
    }

    override suspend fun rejectPendingConfirmation(intakeId: String, code: String) {
        rejected[intakeId] = code
    }
}
