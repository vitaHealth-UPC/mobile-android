package com.vitahealth.tata.omission.infrastructure.remote

/** Token-only transport input that prevents accidental credential disclosure in diagnostics. */
class RegisterPushDeviceRequest(val token: String) {
    override fun toString(): String = "RegisterPushDeviceRequest[redacted]"
}
