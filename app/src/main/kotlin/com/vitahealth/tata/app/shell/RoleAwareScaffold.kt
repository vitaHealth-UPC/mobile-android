package com.vitahealth.tata.app.shell

enum class TataRole {
    OLDER_ADULT,
    CAREGIVER,
}

data class BottomDestination(
    val route: String,
    val label: String,
)

fun destinationsFor(role: TataRole): List<BottomDestination> =
    when (role) {
        TataRole.OLDER_ADULT -> listOf(
            BottomDestination("home", "Inicio"),
            BottomDestination("medications", "Medicamentos"),
            BottomDestination("schedule", "Agenda"),
            BottomDestination("notes", "Notas"),
            BottomDestination("more", "Más"),
        )

        TataRole.CAREGIVER -> listOf(
            BottomDestination("family", "Inicio"),
            BottomDestination("alerts", "Alertas"),
            BottomDestination("notes", "Notas"),
            BottomDestination("person", "Persona"),
            BottomDestination("more", "Más"),
        )
    }
