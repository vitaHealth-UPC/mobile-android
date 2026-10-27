package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/** Maps validated treatment identity and a recognized lifecycle state. */
internal fun TreatmentResponse.toDomain(status: TreatmentStatus): Treatment {
    require(id.isNotBlank() && olderAdultId.isNotBlank() && name.isNotBlank())
    return Treatment(id = id, olderAdultId = olderAdultId, name = name, status = status)
}

/** Projects optional regimen details without inventing a configuration for a draft. */
internal fun TreatmentResponse.toDetailReadModel(
    status: TreatmentStatus
): TreatmentDetailReadModel {
    require(reminderLeadMinutes == null || reminderLeadMinutes in 0..1440)
    require(followUpDelayMinutes == null || followUpDelayMinutes in 0..1440)
    val treatment = toDomain(status)
    return TreatmentDetailReadModel(
        id = treatment.id,
        olderAdultId = treatment.olderAdultId,
        name = treatment.name,
        status = treatment.status,
        medicationId = medicationId,
        dose = dose,
        frequency = frequency,
        scheduledTimes = scheduledTimes.orEmpty(),
        instructions = instructions.orEmpty(),
        reminderLeadMinutes = reminderLeadMinutes,
        followUpDelayMinutes = followUpDelayMinutes,
    )
}
