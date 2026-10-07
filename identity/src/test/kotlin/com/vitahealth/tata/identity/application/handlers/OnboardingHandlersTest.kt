package com.vitahealth.tata.identity.application.handlers

import com.vitahealth.tata.identity.application.OnboardingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingHandlersTest {
    private class InMemoryOnboarding(var seen: Boolean = false) : OnboardingRepository {
        var writes = 0
        override fun hasSeenOnboarding() = seen
        override fun markOnboardingSeen() {
            writes++
            seen = true
        }
    }

    @Test
    fun theFirstLaunchHasNotSeenTheWelcomeScreen() {
        assertFalse(GetOnboardingStatusQueryHandler(InMemoryOnboarding())())
    }

    @Test
    fun completingItRemembersItForTheNextLaunch() {
        val repository = InMemoryOnboarding()

        CompleteOnboardingCommandHandler(repository)()

        assertTrue(GetOnboardingStatusQueryHandler(repository)())
    }

    @Test
    fun completingItTwiceStoresItOnlyOnce() {
        val repository = InMemoryOnboarding()
        val complete = CompleteOnboardingCommandHandler(repository)

        complete()
        complete()

        assertEquals(1, repository.writes)
    }

    @Test
    fun anAlreadySeenScreenStaysSeen() {
        val repository = InMemoryOnboarding(seen = true)

        CompleteOnboardingCommandHandler(repository)()

        assertTrue(repository.seen)
        assertEquals(0, repository.writes)
    }
}
