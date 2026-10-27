package com.vitahealth.tata.treatment.infrastructure.remote

import com.vitahealth.tata.treatment.domain.model.entities.Medication

/** Converts medication resources only when their identity and required details are present. */
internal fun MedicationResponse.toDomain(): Medication {
    require(id.isNotBlank() && olderAdultId.isNotBlank())
    require(name.isNotBlank() && presentation.isNotBlank())
    return Medication(
        id = id,
        olderAdultId = olderAdultId,
        name = name,
        presentation = presentation,
        active = requireNotNull(active) { "Missing medication availability" },
    )
}
