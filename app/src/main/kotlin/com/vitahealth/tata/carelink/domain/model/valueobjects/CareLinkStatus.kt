package com.vitahealth.tata.carelink.domain.model.valueobjects

/** Lifecycle states exposed by the care-link contract. */
enum class CareLinkStatus {
    PENDING,
    AWAITING_CONSENT,
    CONFIRMED,
    REVOKED,
}
