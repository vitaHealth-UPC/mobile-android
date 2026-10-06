package com.vitahealth.tata.app.navigation

sealed interface RootDestination {
    val route: String

    data object CaregiverRegistration : RootDestination {
        override val route: String = "caregiver-registration"
    }
}
