package com.vitahealth.tata.monitoring.infrastructure.remote

/** Contact channel resource; supported channel types are PHONE and WHATSAPP. */
data class ContactResponse(val type: String?, val value: String?)
