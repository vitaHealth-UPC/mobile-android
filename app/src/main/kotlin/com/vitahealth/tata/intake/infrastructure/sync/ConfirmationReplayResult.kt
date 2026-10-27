package com.vitahealth.tata.intake.infrastructure.sync

/** Outcome interpreted by the scheduler without coupling replay logic to WorkManager. */
enum class ConfirmationReplayResult {
    COMPLETE,
    RETRY,
    AUTHORIZATION_REQUIRED,
    SESSION_CHANGED,
}
