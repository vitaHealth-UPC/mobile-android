package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.infrastructure.local.room.CachedIntakeEntity
import com.vitahealth.tata.intake.infrastructure.local.room.IntakeDao
import com.vitahealth.tata.intake.infrastructure.local.room.PendingConfirmationEntity
import com.vitahealth.tata.shared.application.session.SessionOwner
import java.time.Instant

/** Adapts Room operations to a fixed account partition captured for one authenticated operation. */
class RoomIntakeLocalStore(private val dao: IntakeDao, owner: SessionOwner) : IntakeLocalStore {
    private val ownerKey = owner.storageKey

    override suspend fun cacheDose(dose: DoseDetailReadModel) = dao.saveDose(dose.toEntity())

    override suspend fun acknowledgeConfirmation(dose: DoseDetailReadModel) =
        dao.acknowledge(dose.toEntity())

    override suspend fun replaceAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
        doses: List<DoseDetailReadModel>,
    ) =
        dao.replaceAgenda(
            ownerKey,
            olderAdultId,
            from.toEpochMilli(),
            to.toEpochMilli(),
            doses.map { it.toEntity() },
        )

    override suspend fun clearFutureDoses(olderAdultId: String, from: Instant) =
        dao.clearFuture(ownerKey, olderAdultId, from.toEpochMilli())

    override suspend fun findDose(intakeId: String) = dao.dose(ownerKey, intakeId)?.toReadModel()

    override suspend fun findNextDose(olderAdultId: String, from: Instant) =
        dao.next(ownerKey, olderAdultId, from.toEpochMilli())?.toReadModel()

    override suspend fun findAgenda(olderAdultId: String, from: Instant, to: Instant) =
        dao.agenda(ownerKey, olderAdultId, from.toEpochMilli(), to.toEpochMilli()).map {
            it.toReadModel()
        }

    override suspend fun enqueueConfirmation(command: ConfirmDoseCommand) =
        dao.enqueue(
            PendingConfirmationEntity(
                ownerKey,
                command.intakeId,
                command.channel.name,
                System.currentTimeMillis(),
            )
        )

    override suspend fun pendingConfirmations(): List<ConfirmDoseCommand> = buildList {
        for (row in dao.pending(ownerKey)) {
            val channel = ConfirmationChannel.entries.firstOrNull { it.name == row.channel }
            if (channel == null) {
                dao.rejectPending(ownerKey, row.intakeId, "INVALID_CONFIRMATION_CHANNEL")
            } else {
                add(ConfirmDoseCommand(row.intakeId, channel))
            }
        }
    }

    override suspend fun removePendingConfirmation(intakeId: String) =
        dao.removePending(ownerKey, intakeId)

    override suspend fun rejectPendingConfirmation(intakeId: String, code: String) =
        dao.rejectPending(ownerKey, intakeId, code)

    private fun DoseDetailReadModel.toEntity() =
        CachedIntakeEntity(
            ownerKey,
            id,
            treatmentId,
            medicationId,
            olderAdultId,
            medicationName,
            dose,
            instructions,
            scheduledAt.toEpochMilli(),
            status.name,
            confirmedAt?.toEpochMilli(),
        )

    private fun CachedIntakeEntity.toReadModel() =
        DoseDetailReadModel(
            id,
            treatmentId,
            medicationId,
            olderAdultId,
            medicationName,
            dose,
            instructions,
            Instant.ofEpochMilli(scheduledAt),
            DoseStatus.valueOf(status),
            confirmedAt?.let(Instant::ofEpochMilli),
        )
}
