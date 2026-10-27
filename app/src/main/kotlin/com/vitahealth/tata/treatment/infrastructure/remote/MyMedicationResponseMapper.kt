package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.treatment.application.readmodels.MyMedication
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/**
 * Validates medication ownership and related treatment identities before projecting the catalog.
 */
internal fun MyMedicationResponse.toReadModel(): MyMedication {
    val medication = medication.toDomain()
    require(treatments.map { it.id }.distinct().size == treatments.size)
    val details =
        treatments.map { resource ->
            val status = TreatmentStatus.valueOf(resource.status)
            require(resource.olderAdultId == medication.olderAdultId)
            require(resource.medicationId == null || resource.medicationId == medication.id)
            resource.toDetailReadModel(status)
        }
    val regimen =
        details.firstOrNull { it.status == TreatmentStatus.ACTIVE } ?: details.firstOrNull()
    return MyMedication(
        id = medication.id,
        name = medication.name,
        presentation = medication.presentation,
        active = medication.active && details.any { it.status == TreatmentStatus.ACTIVE },
        dose = regimen?.dose.orEmpty(),
        instructions = regimen?.instructions.orEmpty(),
        scheduledTimes = regimen?.scheduledTimes.orEmpty().toList(),
    )
}
