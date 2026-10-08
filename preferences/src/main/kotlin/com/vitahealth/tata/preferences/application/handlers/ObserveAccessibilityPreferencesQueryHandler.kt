package com.vitahealth.tata.preferences.application.handlers

import com.vitahealth.tata.preferences.application.UserPreferencesRepository
import com.vitahealth.tata.preferences.application.queries.ObserveAccessibilityPreferencesQuery
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import kotlinx.coroutines.flow.Flow

class ObserveAccessibilityPreferencesQueryHandler(
    private val repository: UserPreferencesRepository,
) {
    operator fun invoke(
        @Suppress("UNUSED_PARAMETER") query: ObserveAccessibilityPreferencesQuery = ObserveAccessibilityPreferencesQuery,
    ): Flow<AccessibilityPreferences> = repository.observeAccessibility()
}
