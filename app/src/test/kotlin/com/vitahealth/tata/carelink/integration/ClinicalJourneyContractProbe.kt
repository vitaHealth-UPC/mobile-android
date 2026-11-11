package com.vitahealth.tata.carelink.integration

import com.vitahealth.tata.intake.application.commands.ConfirmDoseCommand
import com.vitahealth.tata.intake.domain.model.valueobjects.ConfirmationChannel
import com.vitahealth.tata.intake.infrastructure.remote.IntakeApiService
import com.vitahealth.tata.intake.infrastructure.remote.RemoteDoseConfirmationRepository
import com.vitahealth.tata.inventory.infrastructure.remote.InventoryApiService
import com.vitahealth.tata.inventory.infrastructure.remote.RemoteInventoryRepository
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentLifecycleRepository
import com.vitahealth.tata.treatment.infrastructure.remote.RemoteTreatmentRepository
import com.vitahealth.tata.treatment.infrastructure.remote.TreatmentApiService
import java.time.LocalDate
import java.time.ZoneOffset
import retrofit2.Retrofit

/** Cross-context receipts over real HTTP; called only by the explicit local-backend probe. */
internal object ClinicalJourneyContractProbe {
    suspend fun verify(
        caregiverClient: Retrofit,
        adultClient: Retrofit,
        caregiver: String,
        adult: String,
    ) {
        val api = caregiverClient.create(TreatmentApiService::class.java)
        val treatment = RemoteTreatmentRepository(api)
        val lifecycle = RemoteTreatmentLifecycleRepository(api)
        val inventory =
            RemoteInventoryRepository(caregiverClient.create(InventoryApiService::class.java))
        val medication =
            treatment
                .registerMedication(caregiver, adult, "Local probe medication", "Tablet")
                .receipt()
        val draft = treatment.createTreatment(caregiver, adult, "Local probe regimen").receipt()
        check(draft.status == TreatmentStatus.DRAFT)
        lifecycle
            .configureTreatment(
                caregiver,
                draft.id,
                medication.id,
                "1 tablet",
                "Daily",
                listOf("23:59"),
                "With water",
                5,
                10,
            )
            .receipt()
        check(
            inventory.registerInitialInventory(medication.id, 10, 3).receipt().remainingStock == 10
        )
        check(
            inventory
                .registerReplenishment(medication.id, 5, "LOCAL-LOT")
                .receipt()
                .remainingStock == 15
        )
        check(
            lifecycle.activateTreatment(caregiver, draft.id).receipt().status ==
                TreatmentStatus.ACTIVE
        )
        val intakes = adultClient.create(IntakeApiService::class.java)
        val day = LocalDate.now(ZoneOffset.UTC)
        val agenda =
            intakes.getAgenda(
                adult,
                day.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString(),
                day.plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC).toString(),
            )
        check(agenda.isSuccessful) { "Agenda HTTP ${agenda.code()}" }
        val dose =
            requireNotNull(agenda.body()).firstOrNull { it.treatmentId == draft.id }
                ?: error("Activated treatment did not generate its agenda")
        val confirmations = RemoteDoseConfirmationRepository(intakes)
        confirmations.confirm(ConfirmDoseCommand(dose.id, ConfirmationChannel.TOUCH)).receipt()
        confirmations.confirm(ConfirmDoseCommand(dose.id, ConfirmationChannel.TOUCH)).receipt()
        check(inventory.getStock(medication.id).receipt().remainingStock == 14) {
            "A confirmation and retry must consume exactly one unit"
        }
        check(
            lifecycle.pauseTreatment(caregiver, draft.id).receipt().status == TreatmentStatus.PAUSED
        )
        check(
            lifecycle.resumeTreatment(caregiver, draft.id).receipt().status ==
                TreatmentStatus.ACTIVE
        )
        PreferencesAnalyticsContractProbe.verify(
            caregiverClient,
            adultClient,
            caregiver,
            adult,
            draft.id,
        )
        println(
            "CLINICAL_JOURNEY_CONTRACT_PASS: treatment, inventory, agenda, confirmation retry, consumption, pause and resume"
        )
    }

    private fun <T> AppResult<T>.receipt(): T =
        when (this) {
            is AppResult.Success -> value
            is AppResult.Failure -> error("Clinical receipt failed: $code")
        }
}
