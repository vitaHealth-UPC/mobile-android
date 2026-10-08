package com.vitahealth.tata.identity.application

/** Remembers on this device whether the welcome screen was already shown. */
interface OnboardingRepository {
    fun hasSeenOnboarding(): Boolean

    fun markOnboardingSeen()
}
