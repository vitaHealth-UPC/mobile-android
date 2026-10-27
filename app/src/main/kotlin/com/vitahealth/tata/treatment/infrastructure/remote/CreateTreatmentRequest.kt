package com.vitahealth.tata.treatment.infrastructure.remote

/** Payload for creating a named treatment draft. */
data class CreateTreatmentRequest(val caregiverId: String, val name: String)
