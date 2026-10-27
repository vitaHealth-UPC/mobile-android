package com.vitahealth.tata.treatment.domain.model.entities

/** Medication identity, owner and current availability for treatment configuration. */
data class Medication(
    val id: String,
    val olderAdultId: String,
    val name: String,
    val presentation: String,
    val active: Boolean,
) {
    init {
        require(id.isNotBlank()) { "Medication requires a valid id" }
        require(olderAdultId.isNotBlank()) { "Medication requires a valid olderAdultId" }
        require(name.isNotBlank()) { "Medication requires a valid name" }
        require(presentation.isNotBlank()) { "Medication requires a valid presentation" }
    }
}
