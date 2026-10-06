package com.vitahealth.tata.app.navigation

sealed interface RootDestination {
    val route: String

    data object Onboarding : RootDestination {
        override val route: String = "onboarding"
    }

    data object CaregiverRegistration : RootDestination {
        override val route: String = "caregiver-registration"
    }

    data object CareLink : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        override val route: String = "care-link/{$caregiverIdArgument}"

        fun createRoute(caregiverId: String): String = "care-link/$caregiverId"
    }
}
