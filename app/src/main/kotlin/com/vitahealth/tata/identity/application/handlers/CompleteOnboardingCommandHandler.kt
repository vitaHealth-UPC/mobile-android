package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.OnboardingRepository
import com.vitahealth.tata.identity.application.commands.CompleteOnboardingCommand

class CompleteOnboardingCommandHandler(
    private val repository: OnboardingRepository,
) {
    operator fun invoke(
        @Suppress("UNUSED_PARAMETER") command: CompleteOnboardingCommand = CompleteOnboardingCommand,
    ) {
        if (!repository.hasSeenOnboarding()) repository.markOnboardingSeen()
    }
}
