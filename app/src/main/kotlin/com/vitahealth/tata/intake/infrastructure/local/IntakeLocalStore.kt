package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import java.time.Instant

interface IntakeLocalStore {
    suspend fun cacheDose(dose: DoseDetailReadModel)

    suspend fun replaceAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
        doses: List<DoseDetailReadModel>,
    )

    suspend fun clearFutureDoses(olderAdultId: String, from: Instant)

    suspend fun findDose(intakeId: String): DoseDetailReadModel?

    suspend fun findNextDose(
        olderAdultId: String,
        from: Instant,
    ): DoseDetailReadModel?

    suspend fun findAgenda(
        olderAdultId: String,
        from: Instant,
        to: Instant,
    ): List<DoseDetailReadModel>

    suspend fun enqueueConfirmation(command: ConfirmDoseCommand)

    suspend fun pendingConfirmations(): List<ConfirmDoseCommand>

    suspend fun removePendingConfirmation(intakeId: String)
}
