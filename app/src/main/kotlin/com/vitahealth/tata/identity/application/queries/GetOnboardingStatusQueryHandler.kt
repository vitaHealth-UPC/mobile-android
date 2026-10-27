package com.vitahealth.tata.identity.application.queries

import com.vitahealth.tata.identity.application.ports.OnboardingRepository
/** Reads whether the installation has already completed the introduction. */
class GetOnboardingStatusQueryHandler(private val repository: OnboardingRepository) {
    /**
     * True when the welcome screen was already seen and the app can open straight on the sign-in.
     */
    operator fun invoke(
        @Suppress("UNUSED_PARAMETER") query: GetOnboardingStatusQuery = GetOnboardingStatusQuery
    ): Boolean = repository.hasSeenOnboarding()
}
