package com.vitahealth.tata.shared.application.session

import kotlinx.coroutines.flow.Flow

/** Observes persisted credential changes, including an initial snapshot when collection begins. */
interface SessionObserver {
    fun observeSessions(): Flow<StoredSession?>
}
