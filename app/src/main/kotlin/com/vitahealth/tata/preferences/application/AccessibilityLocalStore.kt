package com.vitahealth.tata.preferences.application

import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import kotlinx.coroutines.flow.Flow

/** Device copy of the accessibility preferences, so they apply before any network call finishes. */
interface AccessibilityLocalStore {
    fun observe(): Flow<AccessibilityPreferences>

    suspend fun current(): AccessibilityPreferences

    suspend fun save(preferences: AccessibilityPreferences)

    /** True while a change made offline has not reached the backend yet. */
    suspend fun isPendingSync(): Boolean

    suspend fun setPendingSync(pending: Boolean)
}
