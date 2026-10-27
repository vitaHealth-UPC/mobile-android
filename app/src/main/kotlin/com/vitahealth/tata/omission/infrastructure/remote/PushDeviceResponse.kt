package com.vitahealth.tata.omission.infrastructure.remote

/** Backend receipt deliberately excludes the device token and session identity. */
data class PushDeviceResponse(val installationId: String, val updatedAt: String)
