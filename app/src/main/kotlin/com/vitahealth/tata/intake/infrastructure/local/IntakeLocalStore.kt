package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import java.time.Instant

/** Stores server dose snapshots and durable confirmation intent for offline intake flows. */
interface IntakeLocalStore {
    /** Saves the last validated server representation of a single intake. */
    suspend fun cacheDose(dose: DoseDetailReadModel)

    /** Commits an acknowledged server state and clears its queued intent in one transaction. */
    suspend fun acknowledgeConfirmation(dose: DoseDetailReadModel)

    /** Replaces only the requested adult's half-open agenda interval. */
    suspend fun replaceAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
        doses: List<DoseDetailReadModel>,
    )

    /** Removes stale future snapshots when the server reports no upcoming dose. */
    suspend fun clearFutureDoses(olderAdultId: String, from: Instant)

    /** Returns a locally available detail without claiming it is a fresh server response. */
    suspend fun findDose(intakeId: String): DoseDetailReadModel?

    /** Selects the earliest pending cached intake at or after the supplied instant. */
    suspend fun findNextDose(olderAdultId: String, from: Instant): DoseDetailReadModel?

    /** Returns cached snapshots within the requested half-open interval. */
    suspend fun findAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
    ): List<DoseDetailReadModel>

    /** Retains a retryable confirmation intent until its server outcome is handled. */
    suspend fun enqueueConfirmation(command: ConfirmDoseCommand)

    /** Lists durable commands in replay order. */
    suspend fun pendingConfirmations(): List<ConfirmDoseCommand>

    /** Removes a handled pending command without deleting its cached intake. */
    suspend fun removePendingConfirmation(intakeId: String)

    /** Retains a definitive failure without replaying it or silently losing the user's intent. */
    suspend fun rejectPendingConfirmation(intakeId: String, code: String)
}
