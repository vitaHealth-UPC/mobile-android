package com.vitahealth.tata.carelink.infrastructure.remote

/** Caregiver and temporary code submitted to create a link request. */
data class AcceptCareLinkRequest(val caregiverId: String, val code: String)
