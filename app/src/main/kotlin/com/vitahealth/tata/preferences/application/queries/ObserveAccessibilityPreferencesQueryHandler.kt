package com.vitahealth.tata.preferences.application.queries

import com.vitahealth.tata.preferences.application.ports.UserPreferencesRepository
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import kotlinx.coroutines.flow.Flow

class ObserveAccessibilityPreferencesQueryHandler(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(
        @Suppress("UNUSED_PARAMETER")
        query: ObserveAccessibilityPreferencesQuery = ObserveAccessibilityPreferencesQuery
    ): Flow<AccessibilityPreferences> = repository.observeAccessibility()
}
