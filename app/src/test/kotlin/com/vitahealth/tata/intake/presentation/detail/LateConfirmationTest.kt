package com.vitahealth.tata.intake.presentation.detail

import com.vitahealth.tata.intake.application.DoseConfirmationRepository
import com.vitahealth.tata.intake.application.DoseDetailRepository
import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.handlers.ConfirmDoseCommandHandler
import com.vitahealth.tata.intake.application.handlers.GetDoseDetailQueryHandler
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.domain.model.DoseStatus
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** US-23: the dose detail shows the outcome the backend decided for a confirmation. */
class LateConfirmationTest {
    private val pending = DoseDetailReadModel(
        id = "101", treatmentId = "t", medicationId = "m", olderAdultId = "adult-1",
        medicationName = "Losartan 50 mg", dose = "1 tablet", instructions = "",
        scheduledAt = Instant.parse("2026-10-05T13:00:00Z"), status = DoseStatus.PENDING,
    )

    private val details = object : DoseDetailRepository {
        var answers = mutableListOf<AppResult<DoseDetailReadModel>>()
        override suspend fun getDoseDetail(intakeId: String): AppResult<DoseDetailReadModel> =
            if (answers.size > 1) answers.removeAt(0) else answers.first()
    }

    private val confirmations = object : DoseConfirmationRepository {
        var answer: AppResult<DoseDetailReadModel> = AppResult.Success(pending)
        override suspend fun confirm(command: ConfirmDoseCommand): AppResult<DoseDetailReadModel> = answer
    }

    private fun model(): DoseDetailViewModel {
        details.answers = mutableListOf(AppResult.Success(pending))
        return DoseDetailViewModel("101", GetDoseDetailQueryHandler(details), ConfirmDoseCommandHandler(confirmations), Dispatchers.Unconfined)
    }

    private fun content(model: DoseDetailViewModel) = model.state.value as DoseDetailUiState.Content

    @Test fun onTimeConfirmationKeepsTheUsualSuccess() {
        val model = model()
        confirmations.answer = AppResult.Success(pending.copy(status = DoseStatus.CONFIRMED, confirmedAt = Instant.parse("2026-10-05T12:58:00Z")))

        model.confirm()

        assertEquals(ConfirmationOutcome.CONFIRMED, content(model).outcome)
        assertTrue(content(model).confirmationSucceeded)
    }

    @Test fun confirmationWithinToleranceIsShownAsLate() {
        val model = model()
        confirmations.answer = AppResult.Success(pending.copy(status = DoseStatus.LATE, confirmedAt = Instant.parse("2026-10-05T13:02:00Z")))

        model.confirm()

        assertEquals(ConfirmationOutcome.LATE, content(model).outcome)
        assertEquals(Instant.parse("2026-10-05T13:02:00Z"), content(model).dose.confirmedAt)
    }

    @Test fun aDefinitiveOmissionIsPreservedAndReadAgain() {
        val model = model()
        confirmations.answer = AppResult.Failure("Esta toma ya no puede confirmarse.", code = "INTAKE_NOT_CONFIRMABLE")
        details.answers = mutableListOf(AppResult.Success(pending.copy(status = DoseStatus.OMITTED)))

        model.confirm()

        assertEquals(ConfirmationOutcome.OMISSION_PRESERVED, content(model).outcome)
        assertEquals(DoseStatus.OMITTED, content(model).dose.status)
        assertNull(content(model).confirmationMessage)
    }

    @Test fun aFailedReadDoesNotInventAnOmissionOrAllowAnotherConfirmation() {
        val model = model()
        confirmations.answer = AppResult.Failure("Esta toma ya no puede confirmarse.", code = "INTAKE_NOT_CONFIRMABLE")
        details.answers = mutableListOf(AppResult.Failure("No hay conexión.", code = "NETWORK_UNAVAILABLE"))

        model.confirm()

        assertNull(content(model).outcome)
        assertEquals(DoseStatus.PENDING, content(model).dose.status)
        assertTrue(content(model).confirmationUnavailable)
        assertEquals("No hay conexión.", content(model).confirmationMessage)
        confirmations.answer = AppResult.Success(pending.copy(status = DoseStatus.CONFIRMED))
        model.confirm()
        assertEquals(DoseStatus.PENDING, content(model).dose.status)
    }

    @Test fun aDoseConfirmedElsewhereIsNotPresentedAsOmitted() {
        val model = model()
        confirmations.answer = AppResult.Failure("Already confirmed", code = "INTAKE_NOT_CONFIRMABLE")
        details.answers = mutableListOf(AppResult.Success(pending.copy(status = DoseStatus.CONFIRMED)))
        model.confirm()
        assertEquals(DoseStatus.CONFIRMED, content(model).dose.status)
        assertEquals(ConfirmationOutcome.ALREADY_CONFIRMED, content(model).outcome)
        assertTrue(content(model).confirmationSucceeded)
    }

    @Test fun otherFailuresKeepTheDoseWithTheirMessage() {
        val model = model()
        confirmations.answer = AppResult.Failure("No hay conexión.", code = "NETWORK_UNAVAILABLE")

        model.confirm()

        assertNull(content(model).outcome)
        assertEquals("No hay conexión.", content(model).confirmationMessage)
        assertEquals(DoseStatus.PENDING, content(model).dose.status)
    }
    @Test fun nextDoseReadFailureDoesNotUndoSuccessfulConfirmation() {
        details.answers = mutableListOf(AppResult.Success(pending))
        confirmations.answer = AppResult.Success(pending.copy(status = DoseStatus.CONFIRMED, confirmedAt = Instant.now()))
        val next = object : com.vitahealth.tata.intake.application.NextDoseRepository {
            override suspend fun getNextDose(olderAdultId: String): AppResult<com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel?> {
                assertEquals("adult-1", olderAdultId)
                return AppResult.Failure("", code = "NETWORK_UNAVAILABLE")
            }
        }
        val model = DoseDetailViewModel("101", GetDoseDetailQueryHandler(details), ConfirmDoseCommandHandler(confirmations),
            Dispatchers.Unconfined, com.vitahealth.tata.intake.application.handlers.GetNextDoseQueryHandler(next))
        model.confirm()
        assertTrue(content(model).confirmationSucceeded)
        assertNull(content(model).nextDose)
    }

    @Test fun serverMarksAnIdempotentReplayWithoutChangingItsRecordedTime() {
        val model = model()
        val recorded = Instant.parse("2026-10-05T12:58:00Z")
        confirmations.answer = AppResult.Success(pending.copy(status = DoseStatus.CONFIRMED, confirmedAt = recorded, alreadyConfirmed = true))
        model.confirm()
        assertEquals(ConfirmationOutcome.ALREADY_CONFIRMED, content(model).outcome)
        assertEquals(recorded, content(model).dose.confirmedAt)
    }

    @Test fun omissionQueriesNextDoseWithoutUndoingTheRecordedOmission() {
        details.answers = mutableListOf(AppResult.Success(pending))
        confirmations.answer = AppResult.Failure("No longer confirmable", code = "INTAKE_NOT_CONFIRMABLE")
        var requestedOwner: String? = null
        val next = object : com.vitahealth.tata.intake.application.NextDoseRepository {
            override suspend fun getNextDose(olderAdultId: String): AppResult<com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel?> {
                requestedOwner = olderAdultId
                return AppResult.Failure("", code = "NETWORK_UNAVAILABLE")
            }
        }
        val model = DoseDetailViewModel("101", GetDoseDetailQueryHandler(details), ConfirmDoseCommandHandler(confirmations),
            Dispatchers.Unconfined, com.vitahealth.tata.intake.application.handlers.GetNextDoseQueryHandler(next))
        details.answers = mutableListOf(AppResult.Success(pending.copy(status = DoseStatus.OMITTED)))
        model.confirm()
        assertEquals("adult-1", requestedOwner)
        assertEquals(ConfirmationOutcome.OMISSION_PRESERVED, content(model).outcome)
        assertEquals(DoseStatus.OMITTED, content(model).dose.status)
        assertNull(content(model).dose.confirmedAt)
        assertNull(content(model).nextDose)
    }

}
