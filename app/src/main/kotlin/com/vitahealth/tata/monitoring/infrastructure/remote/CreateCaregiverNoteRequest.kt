package com.vitahealth.tata.monitoring.infrastructure.remote

/**
 * `CreateCaregiverNoteResource`: the author goes in the body, there is no `caregiverId` query on
 * POST.
 */
data class CreateCaregiverNoteRequest(val familiarId: String, val text: String)
