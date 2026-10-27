package com.vitahealth.tata.intake.infrastructure.local

import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel

/** Preserves scheduled facts when a next-dose response enters the detail cache. */
internal fun NextDoseReadModel.toDoseDetail() =
    DoseDetailReadModel(
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

/** Projects a cached pending detail without inferring a confirmation outcome. */
internal fun DoseDetailReadModel.toNextDose() =
    NextDoseReadModel(
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

internal const val NETWORK_UNAVAILABLE = "NETWORK_UNAVAILABLE"
internal const val PENDING_SYNC = "PENDING_SYNC"
