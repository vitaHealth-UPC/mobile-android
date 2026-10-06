package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.IntakeAgendaRepository
import com.vitahealth.tata.intake.application.NextDoseRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.sync.SyncScheduler
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstIntakeRepositoriesTest {

    @Test
    fun networkFailureReturnsCachedNextDose() = runBlocking {
        val local = FakeLocalStore().apply {
            cached[dose.id] = dose
        }
        val remote = NextDoseRepository {
            AppResult.Failure(
                message = "offline",
                code = "NETWORK_UNAVAILABLE",
            )
        }
        val repository = OfflineFirstNextDoseRepository(
            remote = remote,
            local = local,
            now = { Instant.parse("2026-10-06T10:00:00Z") },
        )

        val result = repository.getNextDose("adult-1")

        assertTrue(result is AppResult.Success)
        val value = (result as AppResult.Success).value
        assertEquals("intake-1", value?.id)
        assertEquals("medication-1", value?.medicationId)
    }

    @Test
    fun successfulAgendaRefreshReplacesCachedRange() = runBlocking {
        val local = FakeLocalStore()
        val from = Instant.parse("2026-10-06T00:00:00Z")
        val to = Instant.parse("2026-10-07T00:00:00Z")
        val remote = IntakeAgendaRepository { _, _, _ ->
            AppResult.Success(listOf(dose))
        }
        val repository = OfflineFirstIntakeAgendaRepository(
            remote = remote,
            local = local,
        )

        val result = repository.getAgenda("adult-1", from, to)

        assertTrue(result is AppResult.Success)
        assertEquals(listOf(dose), local.lastAgendaReplacement)
        assertEquals("adult-1", local.lastAgendaOlderAdultId)
    }

    @Test
    fun offlineConfirmationIsQueuedAndSchedulesSynchronization() = runBlocking {
        val local = FakeLocalStore().apply {
            cached[dose.id] = dose
        }
        val scheduler = RecordingSyncScheduler()
        val remote = DoseConfirmationRepository {
            AppResult.Failure(
                message = "offline",
                code = "NETWORK_UNAVAILABLE",
            )
        }
        val repository = OfflineFirstDoseConfirmationRepository(
            remote = remote,
            local = local,
            syncScheduler = scheduler,
        )
        val command = ConfirmDoseCommand(
            intakeId = dose.id,
            channel = ConfirmationChannel.TOUCH,
        )

        val first = repository.confirm(command)
        val second = repository.confirm(command)

        assertTrue(first is AppResult.Failure)
        assertEquals("PENDING_SYNC", (first as AppResult.Failure).code)
        assertTrue(second is AppResult.Failure)
        assertEquals(1, local.pending.size)
        assertEquals(command, local.pending[dose.id])
        assertEquals(2, scheduler.scheduleCalls)
    }

    @Test
    fun successfulConfirmationCachesServerStateAndClearsPendingCommand() = runBlocking {
        val local = FakeLocalStore().apply {
            cached[dose.id] = dose
            pending[dose.id] = ConfirmDoseCommand(
                intakeId = dose.id,
                channel = ConfirmationChannel.TOUCH,
            )
        }
        val confirmed = dose.copy(
            status = DoseStatus.CONFIRMED,
            confirmedAt = Instant.parse("2026-10-06T12:01:00Z"),
        )
        val remote = DoseConfirmationRepository {
            AppResult.Success(confirmed)
        }
        val repository = OfflineFirstDoseConfirmationRepository(
            remote = remote,
            local = local,
            syncScheduler = RecordingSyncScheduler(),
        )

        val result = repository.confirm(
            ConfirmDoseCommand(
                intakeId = dose.id,
                channel = ConfirmationChannel.TOUCH,
            ),
        )

        assertTrue(result is AppResult.Success)
        assertEquals(DoseStatus.CONFIRMED, local.cached[dose.id]?.status)
        assertTrue(local.pending.isEmpty())
    }

    @Test
    fun networkFailureReturnsCachedDoseDetail() = runBlocking {
        val local = FakeLocalStore().apply {
            cached[dose.id] = dose
        }
        val remote = DoseDetailRepository {
            AppResult.Failure(
                message = "offline",
                code = "NETWORK_UNAVAILABLE",
            )
        }
        val repository = OfflineFirstDoseDetailRepository(
            remote = remote,
            local = local,
        )

        val result = repository.getDoseDetail(dose.id)

        assertTrue(result is AppResult.Success)
        assertEquals(dose, (result as AppResult.Success).value)
    }

    private class RecordingSyncScheduler : SyncScheduler {
        var scheduleCalls = 0

        override fun schedule() {
            scheduleCalls++
        }
    }

    private class FakeLocalStore : IntakeLocalStore {
        val cached = linkedMapOf<String, DoseDetailReadModel>()
        val pending = linkedMapOf<String, ConfirmDoseCommand>()
        var lastAgendaReplacement: List<DoseDetailReadModel>? = null
        var lastAgendaOlderAdultId: String? = null

        override suspend fun cacheDose(dose: DoseDetailReadModel) {
            cached[dose.id] = dose
        }

        override suspend fun replaceAgenda(
            olderAdultId: String,
            from: Instant,
            to: Instant,
            doses: List<DoseDetailReadModel>,
        ) {
            lastAgendaOlderAdultId = olderAdultId
            lastAgendaReplacement = doses
            cached.entries.removeIf { (_, value) ->
                value.olderAdultId == olderAdultId &&
                    !value.scheduledAt.isBefore(from) &&
                    value.scheduledAt.isBefore(to)
            }
            doses.forEach { cached[it.id] = it }
        }

        override suspend fun clearFutureDoses(
            olderAdultId: String,
            from: Instant,
        ) {
            cached.entries.removeIf { (_, value) ->
                value.olderAdultId == olderAdultId &&
                    !value.scheduledAt.isBefore(from)
            }
        }

        override suspend fun findDose(
            intakeId: String,
        ): DoseDetailReadModel? = cached[intakeId]

        override suspend fun findNextDose(
            olderAdultId: String,
            from: Instant,
        ): DoseDetailReadModel? = cached.values
            .asSequence()
            .filter { it.olderAdultId == olderAdultId }
            .filter { it.status == DoseStatus.PENDING }
            .filter { !it.scheduledAt.isBefore(from) }
            .minWithOrNull(compareBy({ it.scheduledAt }, { it.id }))

        override suspend fun findAgenda(
            olderAdultId: String,
            from: Instant,
            to: Instant,
        ): List<DoseDetailReadModel> = cached.values
            .filter {
                it.olderAdultId == olderAdultId &&
                    !it.scheduledAt.isBefore(from) &&
                    it.scheduledAt.isBefore(to)
            }
            .sortedWith(compareBy({ it.scheduledAt }, { it.id }))

        override suspend fun enqueueConfirmation(command: ConfirmDoseCommand) {
            pending[command.intakeId] = command
        }

        override suspend fun pendingConfirmations(): List<ConfirmDoseCommand> =
            pending.values.toList()

        override suspend fun removePendingConfirmation(intakeId: String) {
            pending.remove(intakeId)
        }
    }

    private companion object {
        val dose = DoseDetailReadModel(
            id = "intake-1",
            treatmentId = "treatment-1",
            medicationId = "medication-1",
            olderAdultId = "adult-1",
            medicationName = "Losartán 50 mg",
            dose = "1 comprimido",
            instructions = "Con agua",
            scheduledAt = Instant.parse("2026-10-06T12:00:00Z"),
            status = DoseStatus.PENDING,
        )
    }
}
