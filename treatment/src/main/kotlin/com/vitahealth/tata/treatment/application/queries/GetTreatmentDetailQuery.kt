package com.vitahealth.tata.treatment.application.queries

data class GetTreatmentDetailQuery(
    val caregiverId: String,
    val treatmentId: String,
)
