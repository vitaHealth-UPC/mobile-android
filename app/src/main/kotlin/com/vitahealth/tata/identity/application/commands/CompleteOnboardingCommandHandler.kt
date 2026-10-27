package com.vitahealth.tata.identity.application.commands

import com.vitahealth.tata.identity.application.ports.OnboardingRepository
/** Records completion of the introduction once for this installation. */
class CompleteOnboardingCommandHandler(private val repository: OnboardingRepository) {
    operator fun invoke(
        @Suppress("UNUSED_PARAMETER") command: CompleteOnboardingCommand = CompleteOnboardingCommand
    ) {
        if (!repository.hasSeenOnboarding()) repository.markOnboardingSeen()
    }
}
