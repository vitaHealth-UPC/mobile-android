package com.vitahealth.tata.treatment.domain.model.entities

import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus

/** Treatment identity, adult ownership and current lifecycle state. */
data class Treatment(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val status: TreatmentStatus,
) {
    init {
        require(id.isNotBlank()) { "Treatment requires a valid id" }
        require(olderAdultId.isNotBlank()) { "Treatment requires a valid olderAdultId" }
        require(name.isNotBlank()) { "Treatment requires a valid name" }
    }
}
