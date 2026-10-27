package com.vitahealth.tata.treatment.application.queries

/** Identifies the caregiver and treatment whose regimen is requested. */
data class GetTreatmentDetailQuery(val caregiverId: String, val treatmentId: String)
