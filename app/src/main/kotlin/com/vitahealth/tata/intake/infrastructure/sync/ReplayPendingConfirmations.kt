package com.vitahealth.tata.intake.infrastructure.sync

import com.vitahealth.tata.intake.application.ports.DoseConfirmationRepository
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.infrastructure.local.IntakeLocalStore
import com.vitahealth.tata.shared.common.result.AppResult

/** Replays one owner's queue using a fixed authenticated adapter and atomic acknowledgements. */
class ReplayPendingConfirmations(
    private val remote: DoseConfirmationRepository,
    private val local: IntakeLocalStore,
    private val sessionIsCurrent: () -> Boolean,
) {
    suspend fun replay(): ConfirmationReplayResult {
        if (!sessionIsCurrent()) return ConfirmationReplayResult.SESSION_CHANGED
        for (command in local.pendingConfirmations()) {
            if (!sessionIsCurrent()) return ConfirmationReplayResult.SESSION_CHANGED
            val result = remote.confirm(command)
            if (!sessionIsCurrent()) return ConfirmationReplayResult.SESSION_CHANGED
            when (result) {
                is AppResult.Success -> {
                    val dose = result.value
                    if (
                        dose.id != command.intakeId ||
                            dose.status !in setOf(DoseStatus.CONFIRMED, DoseStatus.LATE) ||
                            dose.confirmedAt == null
                    ) {
                        return ConfirmationReplayResult.RETRY
                    }
                    local.acknowledgeConfirmation(dose)
                }
                is AppResult.Failure ->
                    when (result.code) {
                        "SESSION_REQUIRED",
                        "AUTHENTICATION_REQUIRED",
                        "ACCESS_DENIED",
                        "CONSENT_REQUIRED" -> return ConfirmationReplayResult.AUTHORIZATION_REQUIRED
                        "INTAKE_NOT_FOUND",
                        "INTAKE_NOT_CONFIRMABLE" ->
                            local.rejectPendingConfirmation(command.intakeId, result.code)
                        else -> return ConfirmationReplayResult.RETRY
                    }
            }
        }
        return ConfirmationReplayResult.COMPLETE
    }
}
