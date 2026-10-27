package com.vitahealth.tata.intake.infrastructure.sync

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.ports.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.infrastructure.local.RecordingIntakeLocalStore
import com.vitahealth.tata.shared.common.result.AppResult
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/** Verifies session changes and error paths retain clinical intent until a valid atomic ACK. */
class ReplayPendingConfirmationsTest {
    private val command = ConfirmDoseCommand("intake", ConfirmationChannel.TOUCH)
    private val acknowledged =
        DoseDetailReadModel(
            "intake",
            "treatment",
            "medication",
            "adult",
            "Losartán",
            "1",
            "",
            Instant.parse("2026-10-06T12:00:00Z"),
            DoseStatus.CONFIRMED,
            Instant.parse("2026-10-06T12:01:00Z"),
        )

    private fun local() = RecordingIntakeLocalStore().apply { pending[command.intakeId] = command }

    private fun remote(result: AppResult<DoseDetailReadModel>) =
        object : DoseConfirmationRepository {
            override suspend fun confirm(command: ConfirmDoseCommand) = result
        }

    @Test
    fun acknowledgedOutcomeIsAppliedOnce() = runBlocking {
        val local = local()
        assertEquals(
            ConfirmationReplayResult.COMPLETE,
            ReplayPendingConfirmations(remote(AppResult.Success(acknowledged)), local) { true }
                .replay(),
        )
        assertEquals(1, local.acknowledgements)
        assertEquals(acknowledged, local.cached["intake"])
        assertTrue(local.pending.isEmpty())
    }

    @Test
    fun accountChangeDuringHttpLeavesThePendingIntentUntouched() = runBlocking {
        val local = local()
        var current = true
        val remote =
            object : DoseConfirmationRepository {
                override suspend fun confirm(
                    command: ConfirmDoseCommand
                ): AppResult<DoseDetailReadModel> {
                    current = false
                    return AppResult.Success(acknowledged)
                }
            }
        assertEquals(
            ConfirmationReplayResult.SESSION_CHANGED,
            ReplayPendingConfirmations(remote, local) { current }.replay(),
        )
        assertEquals(command, local.pending["intake"])
        assertEquals(0, local.acknowledgements)
    }

    @Test
    fun supersededSessionDoesNotSendAnyCommand() = runBlocking {
        val remote =
            object : DoseConfirmationRepository {
                override suspend fun confirm(
                    command: ConfirmDoseCommand
                ): AppResult<DoseDetailReadModel> = error("Must not send")
            }
        assertEquals(
            ConfirmationReplayResult.SESSION_CHANGED,
            ReplayPendingConfirmations(remote, local()) { false }.replay(),
        )
    }

    @Test
    fun retryableFailuresDoNotLoseOrAcknowledgeTheQueue() = runBlocking {
        for (code in listOf("NETWORK_UNAVAILABLE", "INVALID_RESPONSE", "REQUEST_FAILED")) {
            val local = local()
            assertEquals(
                ConfirmationReplayResult.RETRY,
                ReplayPendingConfirmations(
                        remote(AppResult.Failure("rejected", code = code)),
                        local,
                    ) {
                        true
                    }
                    .replay(),
            )
            assertEquals(command, local.pending["intake"])
            assertEquals(0, local.acknowledgements)
        }
    }

    @Test
    fun authorizationFailureWaitsForCredentialsWithoutDeletingIntentOrSendingTheNextCommand() =
        runBlocking {
            for (code in
                listOf(
                    "SESSION_REQUIRED",
                    "AUTHENTICATION_REQUIRED",
                    "ACCESS_DENIED",
                    "CONSENT_REQUIRED",
                )) {
                val local = local().apply { pending["second"] = command.copy(intakeId = "second") }
                var calls = 0
                val remote =
                    object : DoseConfirmationRepository {
                        override suspend fun confirm(
                            command: ConfirmDoseCommand
                        ): AppResult<DoseDetailReadModel> {
                            calls++
                            return AppResult.Failure("authorization required", code = code)
                        }
                    }
                assertEquals(
                    ConfirmationReplayResult.AUTHORIZATION_REQUIRED,
                    ReplayPendingConfirmations(remote, local) { true }.replay(),
                )
                assertEquals(1, calls)
                assertEquals(2, local.pending.size)
                assertEquals(0, local.acknowledgements)
            }
        }

    @Test
    fun definitiveFailureIsRetainedAndDoesNotBlockTheFollowingCommand() = runBlocking {
        val local = local().apply { pending["second"] = command.copy(intakeId = "second") }
        val remote =
            object : DoseConfirmationRepository {
                override suspend fun confirm(
                    command: ConfirmDoseCommand
                ): AppResult<DoseDetailReadModel> =
                    if (command.intakeId == "intake")
                        AppResult.Failure("closed", code = "INTAKE_NOT_CONFIRMABLE")
                    else AppResult.Success(acknowledged.copy(id = "second"))
            }
        assertEquals(
            ConfirmationReplayResult.COMPLETE,
            ReplayPendingConfirmations(remote, local) { true }.replay(),
        )
        assertEquals("INTAKE_NOT_CONFIRMABLE", local.rejected["intake"])
        assertEquals(command, local.pending["intake"])
        assertEquals(1, local.acknowledgements)
        assertTrue(local.pendingConfirmations().isEmpty())
    }

    @Test
    fun wrongOrUnconfirmedAckCannotRemoveTheOriginalCommand() = runBlocking {
        for (dose in
            listOf(
                acknowledged.copy(id = "other"),
                acknowledged.copy(status = DoseStatus.PENDING),
                acknowledged.copy(confirmedAt = null),
            )) {
            val local = local()
            assertEquals(
                ConfirmationReplayResult.RETRY,
                ReplayPendingConfirmations(remote(AppResult.Success(dose)), local) { true }.replay(),
            )
            assertEquals(command, local.pending["intake"])
            assertEquals(0, local.acknowledgements)
        }
    }

    @Test
    fun cancellationRetainsThePendingCommand() = runBlocking {
        val local = local()
        val remote =
            object : DoseConfirmationRepository {
                override suspend fun confirm(
                    command: ConfirmDoseCommand
                ): AppResult<DoseDetailReadModel> = throw CancellationException()
            }
        try {
            ReplayPendingConfirmations(remote, local) { true }.replay()
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertEquals(command, local.pending["intake"])
            assertEquals(0, local.acknowledgements)
        }
    }
}
