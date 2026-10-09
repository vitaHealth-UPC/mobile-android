package com.vitahealth.tata.identity.application

/**
 * Caregiver credentials seeded by the backend when `TATA_DEMO_SEED=true`.
 * Kept in the application layer so presentation can surface them without hard-coding
 * magic strings inside Compose screens.
 */
object DemoCredentials {
    const val EMAIL = "demo@tata.app"
    const val PASSWORD = "Tata-Demo-2026"
}
