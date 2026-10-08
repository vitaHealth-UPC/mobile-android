package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.OnboardingRepository
import com.vitahealth.tata.identity.application.queries.GetOnboardingStatusQuery

class GetOnboardingStatusQueryHandler(
    private val repository: OnboardingRepository,
) {
    /** True when the welcome screen was already seen and the app can open straight on the sign-in. */
    operator fun invoke(
        @Suppress("UNUSED_PARAMETER") query: GetOnboardingStatusQuery = GetOnboardingStatusQuery,
    ): Boolean = repository.hasSeenOnboarding()
}
