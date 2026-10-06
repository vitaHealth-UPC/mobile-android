package com.vitahealth.tata.app.navigation

import android.net.Uri

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

        fun createRoute(caregiverId: String): String = "care-link/" + Uri.encode(caregiverId)
    }

    data object MedicationRegistration : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"

        override val route: String =
            "medication-registration/{$caregiverIdArgument}/{$olderAdultIdArgument}/{$olderAdultNameArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultId: String,
            olderAdultName: String,
        ): String = "medication-registration/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName)
    }

    data object TreatmentCreation : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"
        const val medicationIdArgument = "medicationId"
        const val medicationLabelArgument = "medicationLabel"

        override val route: String =
            "treatment-creation/{$caregiverIdArgument}/{$olderAdultIdArgument}/{$olderAdultNameArgument}/" +
                "{$medicationIdArgument}/{$medicationLabelArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultId: String,
            olderAdultName: String,
            medicationId: String,
            medicationLabel: String,
        ): String = "treatment-creation/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName) + "/" +
            Uri.encode(medicationId) + "/" +
            Uri.encode(medicationLabel)
    }
}
