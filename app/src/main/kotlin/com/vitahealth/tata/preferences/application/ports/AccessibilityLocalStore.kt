package com.vitahealth.tata.preferences.application.ports

import com.vitahealth.tata.preferences.application.sync.AccessibilityField
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import kotlinx.coroutines.flow.Flow

/** Device copy of the accessibility preferences, so they apply before any network call finishes. */
interface AccessibilityLocalStore {
    fun observe(): Flow<AccessibilityPreferences>

    suspend fun current(): AccessibilityPreferences

    suspend fun save(preferences: AccessibilityPreferences)

    /** Atomically writes the local values with their synchronization state. */
    suspend fun saveState(preferences: AccessibilityPreferences, pending: Boolean)

    /** Persists values and the exact unacknowledged field set in one transaction. */
    suspend fun saveState(preferences: AccessibilityPreferences, pending: Set<AccessibilityField>)

    /** Reads pending fields, including compatibility with the older all-fields marker. */
    suspend fun pendingFields(): Set<AccessibilityField>

    /** True while a change made offline has not reached the backend yet. */
    suspend fun isPendingSync(): Boolean

    suspend fun setPendingSync(pending: Boolean)
}
