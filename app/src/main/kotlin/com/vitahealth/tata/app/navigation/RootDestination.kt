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

    data object TreatmentDoseFrequency : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"
        const val medicationIdArgument = "medicationId"
        const val medicationLabelArgument = "medicationLabel"
        const val treatmentIdArgument = "treatmentId"
        const val treatmentNameArgument = "treatmentName"

        override val route: String =
            "treatment-dose-frequency/{$caregiverIdArgument}/{$olderAdultIdArgument}/{$olderAdultNameArgument}/" +
                "{$medicationIdArgument}/{$medicationLabelArgument}/{$treatmentIdArgument}/{$treatmentNameArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultId: String,
            olderAdultName: String,
            medicationId: String,
            medicationLabel: String,
            treatmentId: String,
            treatmentName: String,
        ): String = "treatment-dose-frequency/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName) + "/" +
            Uri.encode(medicationId) + "/" +
            Uri.encode(medicationLabel) + "/" +
            Uri.encode(treatmentId) + "/" +
            Uri.encode(treatmentName)
    }

    data object TreatmentScheduleInstructions : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"
        const val medicationIdArgument = "medicationId"
        const val medicationLabelArgument = "medicationLabel"
        const val treatmentIdArgument = "treatmentId"
        const val treatmentNameArgument = "treatmentName"
        const val dosageArgument = "dosage"
        const val frequencyArgument = "frequency"

        override val route: String =
            "treatment-schedule/{$caregiverIdArgument}/{$olderAdultIdArgument}/{$olderAdultNameArgument}/" +
                "{$medicationIdArgument}/{$medicationLabelArgument}/{$treatmentIdArgument}/{$treatmentNameArgument}/" +
                "{$dosageArgument}/{$frequencyArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultId: String,
            olderAdultName: String,
            medicationId: String,
            medicationLabel: String,
            treatmentId: String,
            treatmentName: String,
            dosage: String,
            frequency: String,
        ): String = "treatment-schedule/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName) + "/" +
            Uri.encode(medicationId) + "/" +
            Uri.encode(medicationLabel) + "/" +
            Uri.encode(treatmentId) + "/" +
            Uri.encode(treatmentName) + "/" +
            Uri.encode(dosage) + "/" +
            Uri.encode(frequency)
    }

    data object TreatmentReminders : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"
        const val medicationIdArgument = "medicationId"
        const val medicationLabelArgument = "medicationLabel"
        const val treatmentIdArgument = "treatmentId"
        const val treatmentNameArgument = "treatmentName"
        const val dosageArgument = "dosage"
        const val frequencyArgument = "frequency"
        const val scheduleTextArgument = "scheduleText"
        const val instructionsArgument = "instructions"

        override val route: String =
            "treatment-reminders/{$caregiverIdArgument}/{$olderAdultIdArgument}/{$olderAdultNameArgument}/" +
                "{$medicationIdArgument}/{$medicationLabelArgument}/{$treatmentIdArgument}/{$treatmentNameArgument}/" +
                "{$dosageArgument}/{$frequencyArgument}/{$scheduleTextArgument}/{$instructionsArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultId: String,
            olderAdultName: String,
            medicationId: String,
            medicationLabel: String,
            treatmentId: String,
            treatmentName: String,
            dosage: String,
            frequency: String,
            scheduleText: String,
            instructions: String,
        ): String = "treatment-reminders/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName) + "/" +
            Uri.encode(medicationId) + "/" +
            Uri.encode(medicationLabel) + "/" +
            Uri.encode(treatmentId) + "/" +
            Uri.encode(treatmentName) + "/" +
            Uri.encode(dosage) + "/" +
            Uri.encode(frequency) + "/" +
            Uri.encode(scheduleText) + "/" +
            Uri.encode(instructions.ifBlank { " " })
    }


    data object TreatmentLifecycle : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"
        const val medicationIdArgument = "medicationId"
        const val medicationLabelArgument = "medicationLabel"
        const val treatmentIdArgument = "treatmentId"
        const val treatmentNameArgument = "treatmentName"
        const val dosageArgument = "dosage"
        const val frequencyArgument = "frequency"
        const val scheduleTextArgument = "scheduleText"
        const val instructionsArgument = "instructions"
        const val reminderDelayArgument = "reminderDelay"

        override val route: String =
            "treatment-lifecycle/{$caregiverIdArgument}/{$olderAdultIdArgument}/{$olderAdultNameArgument}/" +
                "{$medicationIdArgument}/{$medicationLabelArgument}/{$treatmentIdArgument}/{$treatmentNameArgument}/" +
                "{$dosageArgument}/{$frequencyArgument}/{$scheduleTextArgument}/{$instructionsArgument}/{$reminderDelayArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultId: String,
            olderAdultName: String,
            medicationId: String,
            medicationLabel: String,
            treatmentId: String,
            treatmentName: String,
            dosage: String,
            frequency: String,
            scheduleText: String,
            instructions: String,
            reminderDelay: Int,
        ): String = "treatment-lifecycle/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName) + "/" +
            Uri.encode(medicationId) + "/" +
            Uri.encode(medicationLabel) + "/" +
            Uri.encode(treatmentId) + "/" +
            Uri.encode(treatmentName) + "/" +
            Uri.encode(dosage) + "/" +
            Uri.encode(frequency) + "/" +
            Uri.encode(scheduleText) + "/" +
            Uri.encode(instructions.ifBlank { " " }) + "/" +
            reminderDelay.toString()
    }


    data object TreatmentDetail : RootDestination {
        const val caregiverIdArgument = "caregiverId"
        const val olderAdultNameArgument = "olderAdultName"
        const val treatmentIdArgument = "treatmentId"
        const val medicationLabelHintArgument = "medicationLabelHint"

        override val route: String =
            "treatment-detail/{$caregiverIdArgument}/{$olderAdultNameArgument}/{$treatmentIdArgument}/{$medicationLabelHintArgument}"

        fun createRoute(
            caregiverId: String,
            olderAdultName: String,
            treatmentId: String,
            medicationLabelHint: String,
        ): String = "treatment-detail/" +
            Uri.encode(caregiverId) + "/" +
            Uri.encode(olderAdultName) + "/" +
            Uri.encode(treatmentId) + "/" +
            Uri.encode(medicationLabelHint.ifBlank { " " })
    }

    data object NextDoseHome : RootDestination {
        const val olderAdultIdArgument = "olderAdultId"
        const val olderAdultNameArgument = "olderAdultName"

        override val route: String =
            "older-adult-home/{$olderAdultIdArgument}/{$olderAdultNameArgument}"

        fun createRoute(
            olderAdultId: String,
            olderAdultName: String,
        ): String = "older-adult-home/" +
            Uri.encode(olderAdultId) + "/" +
            Uri.encode(olderAdultName)
    }

    data object DoseDetail : RootDestination {
        const val intakeIdArgument = "intakeId"
        override val route: String = "dose-detail/{$intakeIdArgument}"

        fun createRoute(intakeId: String): String =
            "dose-detail/" + Uri.encode(intakeId)
    }

    data object AdherenceHistory : RootDestination {
        const val olderAdultIdArgument = "olderAdultId"
        override val route: String = "adherence-history/{$olderAdultIdArgument}"

        fun createRoute(olderAdultId: String): String =
            "adherence-history/" + Uri.encode(olderAdultId)
    }

    data object AdherenceRecommendations : RootDestination {
        const val olderAdultIdArgument = "olderAdultId"
        override val route: String = "adherence-recommendations/{$olderAdultIdArgument}"

        fun createRoute(olderAdultId: String): String =
            "adherence-recommendations/" + Uri.encode(olderAdultId)
    }

}
