package com.vitahealth.tata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.analytics.application.readmodels.*
import com.vitahealth.tata.analytics.presentation.history.screens.AdherenceHistoryScreen
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceSummaryUi
import com.vitahealth.tata.analytics.presentation.history.ui.AdherenceTrendPoint
import com.vitahealth.tata.analytics.presentation.history.ui.PatternUi
import com.vitahealth.tata.analytics.presentation.history.ui.RecentIntakeUi
import com.vitahealth.tata.analytics.presentation.history.viewmodel.AdherenceHistoryUiState
import com.vitahealth.tata.analytics.presentation.recommendations.screens.AdherenceRecommendationsScreen
import com.vitahealth.tata.analytics.presentation.recommendations.ui.AdherenceRecommendationsUi
import com.vitahealth.tata.analytics.presentation.recommendations.ui.RecommendationUi
import com.vitahealth.tata.analytics.presentation.recommendations.viewmodel.AdherenceRecommendationsUiState
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.presentation.link.screens.CareLinkScreen
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkUiState
import com.vitahealth.tata.carelink.presentation.profiles.screens.CaregiverProfilesScreen
import com.vitahealth.tata.carelink.presentation.profiles.viewmodel.CaregiverProfilesState
import com.vitahealth.tata.identity.presentation.access.screens.PinAccessScreen
import com.vitahealth.tata.identity.presentation.access.screens.SessionAccessScreen
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessUiState
import com.vitahealth.tata.identity.presentation.registration.screens.CaregiverRegistrationScreen
import com.vitahealth.tata.identity.presentation.registration.viewmodel.CaregiverRegistrationUiState
import com.vitahealth.tata.identity.presentation.registration.viewmodel.RegistrationStep
import com.vitahealth.tata.identity.presentation.subscription.screens.PlanSubscriptionScreen
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanMessage
import com.vitahealth.tata.identity.presentation.subscription.viewmodel.PlanSubscriptionUiState
import com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen
import com.vitahealth.tata.intake.presentation.detail.screens.DoseOutcomeScreen
import com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
import com.vitahealth.tata.intake.presentation.home.screens.NextDoseHomeScreen
import com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
import com.vitahealth.tata.intake.presentation.voice.screens.VoiceConfirmationScreen
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoiceConfirmationUiState
import com.vitahealth.tata.intake.presentation.voice.viewmodel.VoicePhase
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.entities.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.valueobjects.StockStatus
import com.vitahealth.tata.inventory.presentation.inventory.screens.InventoryScreen
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import com.vitahealth.tata.monitoring.application.readmodels.FamilySummary
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose
import com.vitahealth.tata.monitoring.application.readmodels.OpenAlert
import com.vitahealth.tata.monitoring.application.readmodels.StockAttention
import com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.domain.model.entities.PersonalNote
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AdherenceCounts
import com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus
import com.vitahealth.tata.monitoring.domain.model.valueobjects.PersonalNoteCategory
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertDetailScreen
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertsScreen
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailUiState
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsUiState
import com.vitahealth.tata.monitoring.presentation.notes.screens.NotesScreen
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
import com.vitahealth.tata.monitoring.presentation.personalnotes.screens.PersonalNotesScreen
import com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel.PersonalNotesUiState
import com.vitahealth.tata.monitoring.presentation.summary.screens.FamilySummaryScreen
import com.vitahealth.tata.monitoring.presentation.summary.viewmodel.FamilySummaryUiState
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.presentation.accessibility.screens.AccessibilityScreen
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityMessage
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityUiState
import com.vitahealth.tata.preferences.presentation.notifications.screens.NotificationPreferencesScreen
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.medication.screens.MedicationManagementScreen
import com.vitahealth.tata.treatment.presentation.medication.screens.MedicationRegistrationScreen
import com.vitahealth.tata.treatment.presentation.medication.screens.MyMedicationsScreen
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementMessage
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementUiState
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationRegistrationUiState
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MyMedicationsUiState
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentListRoute
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentCreationScreen
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentLifecycleScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentCreationUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentListViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated presentation fixtures. No authentication or production requests. */
@RunWith(AndroidJUnit4::class)
class ExistingScreensVisualAuditTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun captureVoiceStates() {
        val now = java.time.Instant.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "voice",
                "t",
                "med",
                "adult",
                "Losartán cincuenta miligramos",
                "1 comprimido",
                "",
                now,
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        var phase by
            mutableStateOf(
                com.vitahealth.tata.intake.presentation.voice.viewmodel.VoicePhase.RECORDING
            )
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.voice.screens.VoiceConfirmationScreen(
                        com.vitahealth.tata.intake.presentation.voice.viewmodel
                            .VoiceConfirmationUiState(phase, dose),
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Escuchando...").assertExists()
        capture("voice-listening", composeOnly = true)
        compose.runOnIdle {
            phase =
                com.vitahealth.tata.intake.presentation.voice.viewmodel.VoicePhase.NOT_RECOGNIZED
        }
        compose.onNodeWithText("No reconocida").assertExists()
        compose.onNodeWithText("Confirmación no registrada").assertExists()
        capture("voice-not-recognized", composeOnly = true)
    }

    @Test
    fun captureReinforcedReminderAndItsRemoval() {
        val now = java.time.Instant.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "reminder",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 tableta",
                "",
                now.minusSeconds(60),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        var state by
            mutableStateOf<
                com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
            >(
                com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
                    .NextDoseAvailable(
                        dose,
                        "Rosa Vargas",
                        com.vitahealth.tata.intake.application.readmodels.DailyDoseProgress(2, 3),
                    )
            )
        var voiceIntake: String? = null
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.home.screens.NextDoseHomeScreen(
                        state,
                        {},
                        {},
                        {},
                        onOpenVoice = { voiceIntake = it },
                    )
                }
            }
        }
        compose.onNodeWithText("Segundo recordatorio").assertExists()
        capture("reinforced-reminder", composeOnly = true)
        compose.onNodeWithText("Confirmar\ncon voz").performClick()
        compose.runOnIdle {
            check(voiceIntake == "reminder")
            state =
                com.vitahealth.tata.intake.presentation.home.viewmodel.NextDoseHomeUiState
                    .NoNextDose("Rosa Vargas")
        }
        compose.onNodeWithText("Segundo recordatorio").assertDoesNotExist()
    }

    @Test
    fun capturePendingAndConfirmedDoseDetails() {
        val now = java.time.Instant.now()
        var status by
            mutableStateOf(com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING)
        val next =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "next",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 comprimido",
                "",
                now.plusSeconds(86400),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        var confirmations = 0
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    val dose =
                        com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                            "detail",
                            "t",
                            "med",
                            "adult",
                            "Losartán 50 mg",
                            "1 comprimido",
                            "Con o sin alimentos",
                            now,
                            status,
                        )
                    key(status) {
                        com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen(
                            com.vitahealth.tata.intake.presentation.detail.viewmodel
                                .DoseDetailUiState
                                .Content(
                                    dose,
                                    nextDose =
                                        next.takeIf {
                                            status !=
                                                com.vitahealth.tata.intake.domain.model.valueobjects
                                                    .DoseStatus
                                                    .PENDING
                                        },
                                ),
                            {},
                            {},
                            { confirmations++ },
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Estado: Pendiente").assertExists()
        capture("dose-detail-pending", composeOnly = true)
        compose.onNodeWithText("Confirmar toma").performScrollTo().performClick()
        compose.runOnIdle {
            check(confirmations == 1)
            status = com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.CONFIRMED
        }
        compose.onNodeWithText("Estado: Confirmada").assertExists()
        compose.onNodeWithText("Confirmar toma").assertDoesNotExist()
        capture("dose-detail-confirmed", composeOnly = true)
    }

    @Test
    fun captureRecordedDoseDetails() {
        val now = java.time.Instant.now()
        val next =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "next",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 comprimido",
                "",
                now.plusSeconds(86400),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        var status by
            mutableStateOf(com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.LATE)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    val dose =
                        com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                            "detail",
                            "t",
                            "med",
                            "adult",
                            "Losartán 50 mg",
                            "1 comprimido",
                            "Con o sin alimentos",
                            now,
                            status,
                        )
                    com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen(
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
                            .Content(dose, nextDose = next),
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Estado: Tardía").assertExists()
        capture("dose-detail-late", composeOnly = true)
        compose.runOnIdle {
            status = com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.OMITTED
        }
        compose.onNodeWithText("Estado: Omitida").assertExists()
        capture("dose-detail-omitted", composeOnly = true)
    }

    @Test
    fun captureOmissionPreserved() {
        val now = java.time.Instant.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "omitted",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 comprimido",
                "",
                now.minusSeconds(3600),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.OMITTED,
            )
        val next =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "next",
                "t2",
                "med2",
                "adult",
                "Metformina 850 mg",
                "1 comprimido",
                "",
                now.plusSeconds(3600),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen(
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
                            .Content(
                                dose,
                                nextDose = next,
                                outcome =
                                    com.vitahealth.tata.intake.presentation.detail.viewmodel
                                        .ConfirmationOutcome
                                        .OMISSION_PRESERVED,
                            ),
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Toma omitida").assertExists()
        compose.onNodeWithText("Confirmación no disponible").assertExists()
        compose.onNodeWithText("Periodo finalizado").assertExists()
        capture("omission-preserved", composeOnly = true)
    }

    @Test
    fun captureLateDoseConfirmed() {
        val now = java.time.Instant.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "late",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 comprimido",
                "",
                now.minusSeconds(300),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.LATE,
                now,
            )
        val next =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "next",
                "t2",
                "med2",
                "adult",
                "Metformina 850 mg",
                "1 comprimido",
                "",
                now.plusSeconds(3600),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.detail.screens.DoseDetailScreen(
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
                            .Content(
                                dose,
                                nextDose = next,
                                confirmationSucceeded = true,
                                outcome =
                                    com.vitahealth.tata.intake.presentation.detail.viewmodel
                                        .ConfirmationOutcome
                                        .LATE,
                            ),
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Toma confirmada con retraso").assertExists()
        compose.onNodeWithText("Clasificada como tardía").assertExists()
        compose.onNodeWithText("Metformina 850 mg").assertExists()
        capture("late-dose-confirmed", composeOnly = true)
    }

    @Test
    fun captureDoseAlreadyConfirmed() {
        val now = java.time.Instant.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "done",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 comprimido",
                "",
                now,
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.CONFIRMED,
                now,
                alreadyConfirmed = true,
            )
        val next =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "next",
                "t2",
                "med2",
                "adult",
                "Metformina 850 mg",
                "1 comprimido",
                "",
                now.plusSeconds(3600),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.detail.screens.DoseOutcomeScreen(
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
                            .Content(
                                dose,
                                nextDose = next,
                                confirmationSucceeded = true,
                                outcome =
                                    com.vitahealth.tata.intake.presentation.detail.viewmodel
                                        .ConfirmationOutcome
                                        .ALREADY_CONFIRMED,
                            ),
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Toma ya confirmada").assertExists()
        compose.onNodeWithText("Sin duplicados").assertExists()
        capture("dose-already-confirmed", composeOnly = true)
    }

    @Test
    fun captureDoseConfirmed() {
        val now = java.time.Instant.now()
        val dose =
            com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel(
                "done",
                "t",
                "med",
                "adult",
                "Losartán 50 mg",
                "1 comprimido",
                "",
                now,
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.CONFIRMED,
                now,
            )
        val next =
            com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel(
                "next",
                "t2",
                "med2",
                "adult",
                "Metformina 850 mg",
                "1 comprimido",
                "",
                now.plusSeconds(3600),
                com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus.PENDING,
            )
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.intake.presentation.detail.screens.DoseOutcomeScreen(
                        com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
                            .Content(dose, nextDose = next, confirmationSucceeded = true),
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("¡Bien hecho!").assertExists()
        capture("dose-confirmed", composeOnly = true)
    }

    @Test
    fun capturePersonalNotesAndFilter() {
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now()
        fun date(days: Long, hour: Int, minute: Int) =
            today.minusDays(days).atTime(hour, minute).atZone(zone).toInstant()
        val notes =
            listOf(
                PersonalNote(
                    1,
                    "Tomar con agua",
                    "Losartán 50 mg. Mantener el mismo horario y preparar un vaso de agua antes de la toma.",
                    PersonalNoteCategory.MEDICATION,
                    date(0, 8, 5),
                ),
                PersonalNote(
                    2,
                    "Antes de salir",
                    "Revisar las próximas tomas si voy a estar fuera de casa.",
                    PersonalNoteCategory.ROUTINE,
                    date(1, 17, 40),
                ),
                PersonalNote(
                    3,
                    "Rutina de la noche",
                    "Dejar el medicamento junto al vaso de agua.",
                    PersonalNoteCategory.ROUTINE,
                    date(2, 21, 10),
                ),
            )
        var category by mutableStateOf<PersonalNoteCategory?>(null)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.monitoring.presentation.personalnotes.screens
                        .PersonalNotesScreen(
                            com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel
                                .PersonalNotesUiState(
                                    notes = notes,
                                    loading = false,
                                    category = category,
                                ),
                            {},
                            { category = it },
                            {},
                            {},
                            {},
                        )
                }
            }
        }
        capture("personal-notes", composeOnly = true)
        compose.onNodeWithText("Rutina").performClick()
        compose.onNodeWithText("Antes de salir").assertExists()
        capture("personal-notes-routine", composeOnly = true)
    }

    @Test
    fun captureMyMedicationsAndHistory() {
        val medications =
            listOf(
                com.vitahealth.tata.treatment.application.readmodels.MyMedication(
                    "1",
                    "Losartán 50 mg",
                    "Comprimido",
                    true,
                    "1 comprimido",
                    "Cada mañana",
                    listOf("08:00"),
                ),
                com.vitahealth.tata.treatment.application.readmodels.MyMedication(
                    "2",
                    "Metformina\n850 mg",
                    "Comprimido",
                    true,
                    "1 comprimido",
                    "Con el desayuno",
                    listOf("08:00"),
                ),
                com.vitahealth.tata.treatment.application.readmodels.MyMedication(
                    "3",
                    "Atorvastatina\n20 mg",
                    "Comprimido",
                    true,
                    "1 comprimido",
                    "Por la noche",
                    listOf("20:00"),
                ),
                com.vitahealth.tata.treatment.application.readmodels.MyMedication(
                    "4",
                    "Vitamina D3\n1,000 UI",
                    "Cápsula",
                    true,
                    "1 cápsula",
                    "Cada mañana",
                    listOf("08:00"),
                ),
                com.vitahealth.tata.treatment.application.readmodels.MyMedication(
                    "5",
                    "Amlodipino 5 mg",
                    "Comprimido",
                    true,
                    "1 comprimido",
                    "Cada noche",
                    listOf("20:00"),
                ),
            )
        var history by mutableStateOf(false)
        val state =
            MyMedicationsUiState(
                medications = medications,
                loading = false,
                nextDose =
                    com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose(
                        "intake",
                        "1",
                        java.time.Instant.parse("2026-10-08T13:00:00Z"),
                    ),
            )
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    MyMedicationsScreen(
                        state.copy(history = history),
                        {},
                        { history = it },
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        capture("my-medications")
        compose.onNodeWithText("Historial").performClick()
        compose.onNodeWithText("No hay medicamentos en el historial.").assertExists()
        capture("my-medications-history-empty")
        compose.onNodeWithContentDescription("Agregar medicamento").performClick()
        compose
            .onNodeWithText("Tu cuidador puede agregar medicamentos y configurar tus tratamientos.")
            .assertExists()
    }

    @Test
    fun defaultResourcesUseSpanishAndEnglishIsExplicit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        fun localized(tag: String): android.content.Context {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocales(android.os.LocaleList.forLanguageTags(tag))
            return context.createConfigurationContext(config)
        }
        val spanish =
            localized("fr-FR") // An unsupported device language must use the Spanish fallback.
        check(spanish.getString(com.vitahealth.tata.R.string.onboarding_start) == "Comenzar")
        check(spanish.getString(com.vitahealth.tata.R.string.alerts_title) == "Alertas")
        check(spanish.getString(com.vitahealth.tata.R.string.detail_status_late) == "Tardía")
        check(spanish.getString(com.vitahealth.tata.R.string.inventory_title) == "Inventario")
        check(
            spanish.getString(com.vitahealth.tata.R.string.accessibility_title) == "Accesibilidad"
        )
        val english = localized("en-US")
        check(english.getString(com.vitahealth.tata.R.string.onboarding_start) == "Get started")
        check(english.getString(com.vitahealth.tata.R.string.alerts_title) == "Alerts")
        check(english.getString(com.vitahealth.tata.R.string.detail_status_late) == "Late")
        check(english.getString(com.vitahealth.tata.R.string.detail_title) == "Medication detail")
    }

    @Test
    fun subscriptionSessionFailureOffersSignInInsteadOfRetry() {
        var signIns = 0
        var retries = 0
        compose.setContent {
            AuditTheme {
                PlanSubscriptionScreen(
                    state =
                        PlanSubscriptionUiState(
                            isLoading = false,
                            message = PlanMessage.ErrorSession,
                        ),
                    onBack = {},
                    onRetry = { retries++ },
                    onSelectPlan = {},
                    onChangePlan = {},
                    onChangeCancel = {},
                    onChangeConfirm = {},
                    onSignIn = { signIns++ },
                )
            }
        }
        compose.onNodeWithText("Iniciar sesión").performClick()
        compose.runOnIdle { check(signIns == 1 && retries == 0) }
    }

    @Test
    fun captureOnboardingAndPlanVariants() {
        val essential =
            com.vitahealth.tata.identity.domain.model.entities.Plan(
                "ESSENTIAL",
                "Esencial",
                java.math.BigDecimal("9.90"),
                "PEN",
                setOf(
                    com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability.REMINDERS,
                    com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability.AGENDA,
                    com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability
                        .INTAKE_CONFIRMATION,
                ),
            )
        val family =
            essential.copy(
                code = "FAMILY",
                name = "Familiar",
                monthlyPrice = java.math.BigDecimal("19.90"),
                capabilities =
                    com.vitahealth.tata.identity.domain.model.valueobjects.PlanCapability.entries
                        .toSet(),
            )
        val state =
            PlanSubscriptionUiState(
                isLoading = false,
                plans = listOf(essential, family),
                subscription =
                    com.vitahealth.tata.identity.domain.model.entities.Subscription(
                        "account-test",
                        family,
                        com.vitahealth.tata.identity.domain.model.valueobjects.SubscriptionStatus
                            .ACTIVE,
                        java.time.Instant.parse("2026-10-28T00:00:00Z"),
                    ),
            )
        var variant by mutableStateOf(0)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    if (variant == 0)
                        com.vitahealth.tata.identity.presentation.onboarding.screens
                            .OnboardingScreen({}, {})
                    else
                        PlanSubscriptionScreen(
                            state.copy(
                                changeMessage =
                                    if (variant == 2)
                                        com.vitahealth.tata.identity.presentation.subscription
                                            .viewmodel
                                            .PlanChangeMessage
                                            .Updated
                                    else null
                            ),
                            {},
                            {},
                            {},
                            {},
                            {},
                            {},
                        )
                }
            }
        }
        capture("onboarding")
        compose.runOnIdle { variant = 1 }
        compose.onNodeWithText("Plan y suscripción").assertExists()
        capture("plan-subscription", composeOnly = true)
        compose.runOnIdle { variant = 2 }
        compose.onNodeWithText("Suscripción actualizada").assertExists()
        capture("plan-updated", composeOnly = true)
    }

    @Test
    fun captureHistoryVariants() {
        val summary =
            AdherenceSummaryUi(
                "Últimos 30 días",
                92,
                "+4%",
                87,
                "+3%",
                2,
                1,
                "2 tardías · 1 omitida",
                listOf(
                    AdherenceTrendPoint("S1", 80),
                    AdherenceTrendPoint("S2", 87),
                    AdherenceTrendPoint("S3", 90),
                    AdherenceTrendPoint("S4", 92),
                ),
                recentIntakes =
                    listOf(
                        RecentIntakeUi(
                            "Hoy, 8:00 a. m.",
                            "Losartán",
                            "A tiempo",
                            com.vitahealth.tata.analytics.domain.model.valueobjects
                                .IntakeOutcomeStatus
                                .OnTime,
                        )
                    ),
                pattern =
                    PatternUi("Excelente progreso", "Los retrasos se concentran por la tarde."),
            )
        var state by
            mutableStateOf<AdherenceHistoryUiState>(AdherenceHistoryUiState.Content(summary))
        var period by
            mutableStateOf(
                com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod.LastMonth
            )
        compose.setContent {
            AuditTheme {
                AdherenceHistoryScreen(
                    state,
                    Modifier.safeDrawingPadding(),
                    selectedPeriod = period,
                    onPeriodSelected = { period = it },
                )
            }
        }
        capture("history-content")
        compose.onNodeWithText("Últimos 30 días âŒ„").performClick()
        compose.onNodeWithText("Últimos 7 días").performClick()
        compose.runOnIdle {
            check(
                period ==
                    com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod.LastWeek
            )
            period =
                com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod.LastMonth
        }
        compose.runOnIdle { state = AdherenceHistoryUiState.InsufficientData("Últimos 30 días") }
        capture("history-insufficient")
        compose.runOnIdle { state = AdherenceHistoryUiState.NoResults("Últimos 30 días") }
        capture("history-empty")
        compose.runOnIdle {
            period =
                com.vitahealth.tata.analytics.domain.model.valueobjects.AdherencePeriod.LastWeek
            state =
                AdherenceHistoryUiState.Content(
                    summary.copy(periodLabel = "Últimos 7 días"),
                    periodUpdated = true,
                )
        }
        capture("history-period-changed")
    }

    @Test
    fun captureRecommendationsVariants() {
        val data =
            AdherenceRecommendationsUi(
                30,
                "Patrón de omisiones por la tarde",
                "4 omisiones y 6 tomas tardías se concentran entre 6:00 y 9:00 p. m.",
                listOf(
                    listOf(0.27f, 0.50f, 0.72f, 0.42f, 0.34f, 0.80f, 0.64f),
                    listOf(0.27f, 0.42f, 0.64f, 0.57f, 0.34f, 0.72f, 0.80f),
                    listOf(0.19f, 0.34f, 0.57f, 0.50f, 0.27f, 0.64f, 0.72f),
                ),
                listOf(
                    RecommendationUi(
                        "Ajusta el recordatorio",
                        "Prueba avisar 15 minutos antes de la toma.",
                    ),
                    RecommendationUi(
                        "Revisa el horario",
                        "Elige una hora asociada a una rutina estable.",
                    ),
                    RecommendationUi(
                        "Acompaña durante una semana",
                        "Comprueba si el nuevo horario reduce retrasos.",
                    ),
                ),
            )
        var state by
            mutableStateOf<AdherenceRecommendationsUiState>(
                AdherenceRecommendationsUiState.Content(data)
            )
        var applications = 0
        var returns = 0
        compose.setContent {
            AuditTheme {
                key(state::class) {
                    AdherenceRecommendationsScreen(
                        state,
                        Modifier.safeDrawingPadding(),
                        onApplyAdjustments = { applications++ },
                        onBackToHistory = { returns++ },
                    )
                }
            }
        }
        compose.onNodeWithText("Recomendaciones").performScrollTo().assertIsDisplayed()
        capture("recommendations-content", composeOnly = true)
        compose.onNodeWithText("Aplicar ajustes").assertIsDisplayed().performClick()
        compose.runOnIdle { check(applications == 1) }
        compose.runOnIdle { state = AdherenceRecommendationsUiState.InsufficientEvidence }
        compose.onNodeWithText("Recomendaciones").performScrollTo().assertIsDisplayed()
        capture("recommendations-insufficient", composeOnly = true)
        compose.onNodeWithText("Volver a historial").performScrollTo().performClick()
        compose.runOnIdle { check(returns == 1) }
    }

    @Test
    fun captureRecommendationsInsufficientInIsolation() {
        compose.setContent {
            AuditTheme {
                AdherenceRecommendationsScreen(
                    AdherenceRecommendationsUiState.InsufficientEvidence,
                    Modifier.safeDrawingPadding(),
                )
            }
        }
        compose.onNodeWithText("Recomendaciones").assertIsDisplayed()
        compose.onNodeWithText("Volver a historial").assertIsDisplayed()
        capture("recommendations-insufficient-isolated", composeOnly = true)
    }

    @Test
    fun captureLinkVariants() {
        var state by
            mutableStateOf(
                CareLinkUiState(
                    "caregiver-test",
                    code = "TATA-4821",
                    olderAdult =
                        com.vitahealth.tata.carelink.domain.model.entities.OlderAdultProfile(
                            "adult-test",
                            "Rosa Vargas",
                            java.time.LocalDate.of(1958, 5, 12),
                            null,
                            null,
                            null,
                        ),
                )
            )
        compose.setContent {
            AuditTheme {
                CareLinkScreen(
                    state,
                    {},
                    {},
                    {},
                    {},
                    Modifier.safeDrawingPadding(),
                    avatarResource = com.vitahealth.tata.R.drawable.figma_link_avatar,
                )
            }
        }
        capture("link-code")
        compose.runOnIdle {
            state =
                state.copy(code = "INVALIDO", errorMessage = "El código ya venció o fue utilizado.")
        }
        capture("link-invalid")
    }

    @Test
    fun captureLogin() {
        var requested = false
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    SessionAccessScreen(
                        SessionAccessUiState(
                            email = "diego@example.test",
                            password = "fixture-only",
                        ),
                        {},
                        {},
                        { requested = true },
                        {},
                        {},
                        {},
                        showPin = false,
                    )
                }
            }
        }
        compose.onNodeWithText("Iniciar sesión").performClick()
        compose.runOnIdle { check(requested) }
        capture("login")
    }

    @Test
    fun captureNotificationVariants() {
        var state by
            mutableStateOf(
                NotificationPreferencesUiState(
                    isLoading = false,
                    preferences =
                        NotificationPreferences(
                            quietHours =
                                com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
                                    .Default
                        ),
                )
            )
        compose.setContent {
            AuditTheme {
                NotificationPreferencesScreen(
                    state,
                    {},
                    {},
                    {},
                    { _, _ -> },
                    { _, _ -> },
                    { _, _ -> },
                    Modifier.safeDrawingPadding(),
                )
            }
        }
        compose.onNodeWithText("Horario de silencio").assertExists()
        capture("notifications-content", composeOnly = true)
        compose.runOnIdle { state = state.copy(message = NotificationMessage.Saved) }
        capture("notifications-saved", composeOnly = true)
    }

    @Test
    fun notificationChannelEditorPreservesTheSaveCallback() {
        var requested:
            Pair<com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType, Boolean>? =
            null
        val state =
            NotificationPreferencesUiState(
                isLoading = false,
                preferences =
                    NotificationPreferences(
                        quietHours =
                            com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
                                .Default
                    ),
            )
        compose.setContent {
            AuditTheme {
                NotificationPreferencesScreen(
                    state,
                    {},
                    {},
                    {},
                    { _, _ -> },
                    { _, _ -> },
                    { type, enabled -> requested = type to enabled },
                    Modifier.safeDrawingPadding(),
                )
            }
        }
        compose.onNodeWithText("Editar").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNode(hasText("Notificaciones push") and isToggleable()).performSemanticsAction(
            SemanticsActions.OnClick
        ) {
            it()
        }
        compose.runOnIdle {
            check(
                requested ==
                    (com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType.PUSH to
                        false)
            )
        }
        compose.onNode(isDialog()).assertExists()
        compose.onNodeWithText("Entendido").performSemanticsAction(SemanticsActions.OnClick) {
            it()
        }
        compose.onNodeWithText("Editar").assertExists()
        compose.onNodeWithText("Horario de silencio").performSemanticsAction(
            SemanticsActions.OnClick
        ) {
            it()
        }
        compose.onNodeWithText("Desde", substring = true).assertExists()
        compose.onNodeWithText("Hasta", substring = true).assertExists()
        compose.onNode(isDialog()).assertExists()
        compose.onNodeWithText("Entendido").performSemanticsAction(SemanticsActions.OnClick) {
            it()
        }
    }

    @Test
    fun captureFullNotificationPolicyAndEditCadence() {
        val policy =
            com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy(
                10,
                20,
                120,
                180,
                20,
                0,
            )
        val contacts =
            listOf(
                com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact(
                    "Rosa",
                    "+51999123456",
                ),
                com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact(
                    "Diego",
                    "+51988123456",
                ),
            )
        var state by
            mutableStateOf(
                NotificationPreferencesUiState(
                    isLoading = false,
                    preferences =
                        NotificationPreferences(
                            quietHours =
                                com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
                                    .Default,
                            policy = policy,
                            emergencyContacts = contacts,
                        ),
                )
            )
        var requested:
            com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy? =
            null
        compose.setContent {
            AuditTheme {
                NotificationPreferencesScreen(
                    state,
                    {},
                    {},
                    {},
                    { _, _ -> },
                    { _, _ -> },
                    { _, _ -> },
                    Modifier.safeDrawingPadding(),
                    onPolicyChange = { updated, recipients ->
                        check(recipients == contacts)
                        requested = updated
                        state =
                            state.copy(
                                preferences = state.preferences!!.copy(policy = updated),
                                message = NotificationMessage.Saved,
                            )
                    },
                    onTabSelected = {},
                )
            }
        }
        compose.onNodeWithText("Contactos de emergencia", substring = true).assertExists()
        capture("notifications-full-policy", composeOnly = true)
        compose.onNode(hasText("Recordatorios de medicación") and hasClickAction()).performClick()
        compose.onNode(hasText("Cadencia") and hasSetTextAction()).performTextReplacement("15")
        capture("notifications-policy-editor", composeOnly = true, dialog = true)
        compose.onNodeWithText("Guardar").performClick()
        compose.runOnIdle { check(requested?.reminderMinutes == 15) }
        capture("notifications-full-saved", composeOnly = true)
    }

    @Test
    fun captureMedicationRegistrationVariants() {
        var state by
            mutableStateOf(
                MedicationRegistrationUiState(
                    "caregiver-test",
                    "adult-test",
                    "Rosa Vargas",
                    name = "Losartán 50 mg",
                    presentation = "Comprimido",
                    frequency = "Una vez al día",
                    timing = "Con el desayuno",
                )
            )
        compose.setContent {
            AuditTheme {
                MedicationRegistrationScreen(
                    state,
                    {},
                    {},
                    { state = state.copy(frequency = it) },
                    {},
                    {},
                    {},
                    Modifier.safeDrawingPadding(),
                )
            }
        }
        compose.onNodeWithText("Una vez al día").performClick()
        compose.onNodeWithText("Cada 12 horas").performClick()
        compose.runOnIdle { check(state.frequency == "Cada 12 horas") }
        capture("medication-registration")
        compose.runOnIdle {
            state =
                state.copy(
                    name = "",
                    presentation = "",
                    frequency = "",
                    timing = "",
                    errorMessage = "Completa medicamento, dosis y frecuencia antes de continuar.",
                )
        }
        capture("medication-registration-error")
    }

    @Test
    fun captureTreatmentCreationVariants() {
        var state by
            mutableStateOf(
                TreatmentCreationUiState(
                    "caregiver-test",
                    "adult-test",
                    "Rosa Vargas",
                    "med-test",
                    "Losartán 50 mg",
                )
            )
        compose.setContent {
            AuditTheme { TreatmentCreationScreen(state, {}, {}, Modifier.safeDrawingPadding()) }
        }
        capture("treatment-create")
        compose.runOnIdle {
            state = state.copy(errorMessage = "Completa el tratamiento antes de continuar.")
        }
        capture("treatment-incomplete")
    }

    @Test
    fun captureMedicationManagementVariants() {
        val medication = Medication("med-test", "adult-test", "Losartán 50 mg", "Comprimido", true)
        var state by
            mutableStateOf(
                MedicationManagementUiState(
                    "Rosa Vargas",
                    isLoading = false,
                    medications = listOf(medication),
                )
            )
        compose.setContent {
            AuditTheme {
                MedicationManagementScreen(
                    state,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    Modifier.safeDrawingPadding(),
                )
            }
        }
        capture("medication-management")
        compose.runOnIdle { state = state.copy(message = MedicationManagementMessage.Updated) }
        capture("medication-updated")
        compose.runOnIdle {
            state =
                state.copy(
                    message = MedicationManagementMessage.Deactivated,
                    medications = listOf(medication.copy(active = false)),
                )
        }
        capture("medication-deactivated")
        compose.runOnIdle {
            state = state.copy(accessDenied = true, medications = emptyList(), message = null)
        }
        capture("treatment-access-denied")
    }

    @Test
    fun captureTreatmentReviewFigma() {
        val state =
            TreatmentLifecycleUiState(
                "caregiver-test",
                "adult-test",
                "Rosa Vargas",
                "med-test",
                "Losartán 50 mg",
                "treatment-test",
                "Control de presión",
                "1 comprimido",
                "Cada día",
                "08:00,20:00",
                "Con el desayuno",
                15,
                status = TreatmentStatus.DRAFT,
            )
        compose.setContent {
            AuditTheme {
                TreatmentLifecycleScreen(state, {}, {}, {}, {}, Modifier.safeDrawingPadding())
            }
        }
        compose.onNodeWithText("Activar tratamiento").assertIsDisplayed()
        compose.onNodeWithText("Vista previa de agenda").assertIsDisplayed()
        capture("treatment-review-figma", composeOnly = true)
    }

    @Test
    fun captureTreatmentPaused() {
        val state =
            TreatmentLifecycleUiState(
                "caregiver-test",
                "adult-test",
                "Rosa Vargas",
                "med-test",
                "Losartán 50 mg",
                "treatment-test",
                "Control de presión",
                "1 comprimido",
                "Cada día",
                "8:00 a. m.",
                "Con el desayuno",
                15,
                status = TreatmentStatus.PAUSED,
            )
        compose.setContent {
            AuditTheme {
                TreatmentLifecycleScreen(state, {}, {}, {}, {}, Modifier.safeDrawingPadding())
            }
        }
        capture("treatment-paused", composeOnly = true)
    }

    @Test
    fun captureFamilySummaryVariants() {
        val summary =
            FamilySummary(
                "Rosa Vargas",
                java.time.LocalDate.now(),
                AdherenceCounts(12, 14),
                AdherenceCounts(2, 3),
                MonitoredDose(
                    "dose-test",
                    "med-test",
                    "Losartán 50 mg",
                    "1 tableta",
                    java.time.Instant.now().plusSeconds(900),
                    "PENDING",
                ),
                listOf(StockAttention("med-test", "Losartán 50 mg", 5)),
                listOf(OpenAlert(1, "Losartán 50 mg", "Toma pendiente", java.time.Instant.now())),
                true,
            )
        var state by mutableStateOf(FamilySummaryUiState(loading = false, summary = summary))
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    FamilySummaryScreen(state, "Rosa Vargas", {}, {}, {}, {}, {}, {}, {}, {})
                }
            }
        }
        capture("family-summary")
        compose.runOnIdle {
            state =
                FamilySummaryUiState(
                    loading = false,
                    error = "Necesitas consentimiento del adulto mayor.",
                )
        }
        capture("family-consent-required")
    }

    @Test
    fun capturePinVariants() {
        var state by mutableStateOf(SessionAccessUiState(pin = "4821"))
        var setup by mutableStateOf(false)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    PinAccessScreen(state, "Rosa Vargas", setup, {}, {}, {})
                }
            }
        }
        capture("pin-access")
        compose.runOnIdle { setup = true }
        capture("pin-setup")
        compose.runOnIdle {
            setup = false
            state = state.copy(error = "INVALID_PIN")
        }
        capture("pin-incorrect")
        compose.runOnIdle { state = state.copy(error = "PIN_LOCKED") }
        capture("pin-blocked")
    }

    @Test
    fun captureRegistrationVariants() {
        var state by
            mutableStateOf(
                CaregiverRegistrationUiState(
                    name = "Diego Vargas",
                    email = "diego@example.test",
                    password = "Fixture4821",
                )
            )
        compose.setContent {
            AuditTheme {
                CaregiverRegistrationScreen(
                    state,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    Modifier.safeDrawingPadding(),
                )
            }
        }
        capture("registration-content")
        compose.runOnIdle {
            state =
                state.copy(
                    accountId = "account-test",
                    step = RegistrationStep.Verification,
                    verificationCode = "482196",
                )
        }
        capture("registration-verification")
        compose.runOnIdle {
            state =
                state.copy(
                    accountId = null,
                    step = RegistrationStep.Account,
                    verificationCode = "",
                    errorCode = "DUPLICATE_EMAIL",
                    errorMessage = "Este correo ya está registrado.",
                )
        }
        capture("registration-duplicate")
        compose.runOnIdle {
            state =
                state.copy(
                    accountId = "account-test",
                    step = RegistrationStep.VerificationExpired,
                    verificationCode = "482196",
                    errorCode = null,
                    errorMessage = null,
                )
        }
        capture("registration-expired")
    }

    @Test
    fun captureAccessibilityVariants() {
        var state by mutableStateOf(AccessibilityUiState())
        compose.setContent {
            AuditTheme(state.preferences) {
                AccessibilityScreen(
                    state,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    Modifier.safeDrawingPadding(),
                    navigationBar = { AdultTabBar(AdultTab.More, {}, AdultTab.entries.toSet()) },
                )
            }
        }
        capture("accessibility-content", composeOnly = true)
        val variants =
            listOf(
                Triple(
                    "dark-theme",
                    AccessibilityPreferences(darkTheme = true),
                    AccessibilityMessage.DarkThemeSaved,
                ),
                Triple(
                    "large-text",
                    AccessibilityPreferences(textSize = TextSizeLevel.LARGE),
                    AccessibilityMessage.LargeTextSaved,
                ),
                Triple(
                    "high-contrast",
                    AccessibilityPreferences(highContrast = true),
                    AccessibilityMessage.HighContrastSaved,
                ),
                Triple(
                    "reduced-motion",
                    AccessibilityPreferences(reducedMotion = true),
                    AccessibilityMessage.ReducedMotionSaved,
                ),
                Triple(
                    "reading-assistance",
                    AccessibilityPreferences(readingAssistance = true),
                    AccessibilityMessage.ReadingAssistanceSaved,
                ),
            )
        for ((name, preferences, message) in variants) {
            compose.runOnIdle {
                state = AccessibilityUiState(preferences = preferences, message = message)
            }
            capture("accessibility-$name", composeOnly = true)
            val feedback = when (name) {
                "large-text" -> R.string.accessibility_large_text_saved
                "reading-assistance" -> R.string.accessibility_reading_saved
                else -> null
            }
            if (feedback != null) {
                // AuditTheme renders es-419, independently of the test runner locale.
                // Resolve feedback through the same app locale to avoid English-only assertions.
                val context =
                    InstrumentationRegistry.getInstrumentation().targetContext.withSpanishAppLocale()
                compose.onNodeWithText(context.getString(feedback))
                    .performScrollTo().assertIsDisplayed()
                capture("accessibility-$name-feedback", composeOnly = true)
            }
        }
    }

    @Test
    fun captureLinkedPerson() {
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    CaregiverProfilesScreen(
                        CaregiverProfilesState(
                            adults = listOf(LinkedAdult("adult-test", "Rosa Vargas"))
                        ),
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        capture("linked-person")
    }

    @Test
    fun captureConsentRequired() {
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    FamilySummaryScreen(
                        FamilySummaryUiState(
                            loading = false,
                            error = "El vínculo de cuidado ya no está activo.",
                            errorCode = "CARE_RELATIONSHIP_REQUIRED",
                        ),
                        "Rosa Vargas",
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        compose.onNodeWithText("Seguimiento restringido").assertExists()
        compose.onNodeWithText("Adherencia esta semana").assertDoesNotExist()
        capture("consent-required")
    }

    @Test
    fun captureInventoryVariants() {
        val now = java.time.Instant.now()
        val stock =
            InventoryStockReadModel(
                "med-test",
                5,
                7,
                StockStatus.LOW,
                listOf(InventoryBatch("batch-test", 30, now, "Lote 2026-09")),
                now,
                now,
                5,
            )
        var state by
            mutableStateOf(
                InventoryUiState.Ready(
                    stock,
                    "Losartán 50 mg",
                    "comprimidos",
                    replenishmentInput = "30",
                    lotInput = "Lote 2026-09",
                )
            )
        compose.setContent {
            AuditTheme {
                InventoryScreen(
                    state,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    Modifier.safeDrawingPadding(),
                    olderAdultName = "Rosa Vargas",
                )
            }
        }
        capture("inventory-content")
        compose.runOnIdle {
            state =
                state.copy(replenishmentInput = "0", replenishmentErrorCode = "INVALID_QUANTITY")
        }
        capture("inventory-invalid")
        compose.runOnIdle {
            state =
                state.copy(
                    stock =
                        stock.copy(
                            remainingStock = 35,
                            daysRemaining = 35,
                            status = StockStatus.AVAILABLE,
                        ),
                    replenishmentInput = "30",
                    replenishmentErrorCode = null,
                    justRegistered = true,
                )
        }
        capture("inventory-replenished")
    }

    @Test
    fun captureTreatmentList() {
        val repository =
            object : com.vitahealth.tata.treatment.application.ports.TreatmentCatalogRepository {
                override suspend fun listTreatments(caregiverId: String, olderAdultId: String) =
                    com.vitahealth.tata.shared.common.result.AppResult.Success(
                        listOf(
                            com.vitahealth.tata.treatment.application.readmodels
                                .TreatmentDetailReadModel(
                                    "treatment-test",
                                    "adult-test",
                                    "Control de presión",
                                    TreatmentStatus.ACTIVE,
                                    "med-test",
                                    "1 comprimido",
                                    "Cada día",
                                    listOf("08:00"),
                                    "Con agua",
                                    10,
                                )
                        )
                    )
            }
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    TreatmentListRoute(
                        TreatmentListViewModel.Factory(
                            "caregiver-test",
                            "adult-test",
                            com.vitahealth.tata.treatment.application.queries
                                .ListTreatmentsQueryHandler(repository),
                        ),
                        "Rosa Vargas",
                        {},
                        {},
                        {},
                    )
                }
            }
        }
        compose.waitUntil(5_000) {
            compose
                .onAllNodes(androidx.compose.ui.test.hasText("Control de presión"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        capture("treatment-list")
    }

    @Test
    fun captureCaregiverNotesVariants() {
        val note =
            FollowUpNote(
                1,
                "Prefiere recibir una llamada si no responde al segundo recordatorio.",
                java.time.Instant.now(),
                "caregiver-test",
            )
        var saved by mutableStateOf(false)
        var showAlert by mutableStateOf(false)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    if (showAlert)
                        com.vitahealth.tata.monitoring.presentation.alerts.screens
                            .AlertDetailScreen(
                                com.vitahealth.tata.monitoring.presentation.alerts.viewmodel
                                    .AlertDetailUiState
                                    .Content(
                                        CaregiverAlert(
                                            1,
                                            "dose-test",
                                            "Losartán 50 mg",
                                            note.recordedAt,
                                            "Toma pendiente",
                                            AlertStatus.OPEN,
                                            note.recordedAt,
                                            null,
                                        )
                                    ),
                                noteSaved = true,
                            )
                    else
                        com.vitahealth.tata.monitoring.presentation.notes.screens.NotesScreen(
                            com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
                                .Content(listOf(note)),
                            caregiverId = "caregiver-test",
                            noteSaved = saved,
                        )
                }
            }
        }
        capture("caregiver-notes")
        compose.runOnIdle { saved = true }
        capture("caregiver-notes-saved")
        compose.runOnIdle { showAlert = true }
        capture("alert-followup-note-saved")
    }

    @Test
    fun captureAlertTimeline() {
        val now = java.time.Instant.parse("2026-07-20T14:45:00Z")
        val pending =
            com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert(
                1,
                "pending",
                "Losartán 50 mg",
                now.minusSeconds(4500),
                "Hace 1 h 15 min",
                com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus.OPEN,
                now.minusSeconds(4500),
                null,
            )
        val doses =
            listOf(
                com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose(
                    "next",
                    "metformin",
                    "Metformina 500 mg",
                    "1 comprimido",
                    now.plusSeconds(9900),
                    "PENDING",
                ),
                com.vitahealth.tata.monitoring.application.readmodels.MonitoredDose(
                    "confirmed",
                    "thyroid",
                    "Levotiroxina 75 mcg",
                    "1 comprimido",
                    now.minusSeconds(4620),
                    "CONFIRMED",
                ),
            )
        var filter by
            mutableStateOf(
                com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFilter.ALL
            )
        var showPending by mutableStateOf(true)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertsScreen(
                        com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsUiState
                            .Content(
                                alerts = if (showPending) listOf(pending) else emptyList(),
                                filter = filter,
                                doses = doses,
                                olderAdultName = "Rosa Morales",
                                day = java.time.LocalDate.of(2026, 7, 20),
                                asOf = now,
                            ),
                        onFilterSelected = { filter = it },
                    )
                }
            }
        }
        compose.onNodeWithText("Recordatorio próximo").assertIsDisplayed()
        compose.onNodeWithText("Toma confirmada").assertIsDisplayed()
        compose.onNodeWithText("En 2 h 45 min").assertIsDisplayed()
        capture("alerts-timeline-all", composeOnly = true)
        compose.onNodeWithText("Próximas").performClick()
        compose.onNodeWithText("Recordatorio próximo").assertIsDisplayed()
        capture("alerts-timeline-upcoming", composeOnly = true)
        compose.onNodeWithText("Completadas").performClick()
        compose.onNodeWithText("Toma confirmada").assertIsDisplayed()
        capture("alerts-timeline-completed", composeOnly = true)
        compose.runOnIdle {
            showPending = false
            filter = com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFilter.ALL
        }
        compose.onNodeWithText("Sin alertas pendientes").assertIsDisplayed()
        compose.onNodeWithText("Toma confirmada").assertIsDisplayed()
        capture("alerts-timeline-clear", composeOnly = true)
    }

    @Test
    fun captureAlertProfileDetails() {
        val now = java.time.Instant.parse("2026-07-20T13:00:00Z")
        var opened = 0
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertDetailScreen(
                        state =
                            com.vitahealth.tata.monitoring.presentation.alerts.viewmodel
                                .AlertDetailUiState
                                .Content(
                                    com.vitahealth.tata.monitoring.domain.model.entities
                                        .CaregiverAlert(
                                            1,
                                            "intake",
                                            "Losart\u00e1n 50 mg",
                                            now,
                                            "Toma pendiente",
                                            com.vitahealth.tata.monitoring.domain.model.valueobjects
                                                .AlertStatus
                                                .OPEN,
                                            now,
                                            null,
                                        ),
                                    dose =
                                        com.vitahealth.tata.monitoring.application.readmodels
                                            .MonitoredDose(
                                                "intake",
                                                "med",
                                                "Losart\u00e1n 50 mg",
                                                "2 tabletas",
                                                now,
                                                "PENDING",
                                            ),
                                ),
                        person =
                            com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson(
                                "adult",
                                "Rosa Vargas",
                                java.time.LocalDate.of(1958, 7, 20),
                            ),
                        history =
                            listOf(
                                com.vitahealth.tata.monitoring.application.readmodels
                                    .RecordedAlertEvent(
                                        com.vitahealth.tata.monitoring.application.readmodels
                                            .RecordedAlertEvent
                                            .Type
                                            .INTAKE_SCHEDULED,
                                        now,
                                    ),
                                com.vitahealth.tata.monitoring.application.readmodels
                                    .RecordedAlertEvent(
                                        com.vitahealth.tata.monitoring.application.readmodels
                                            .RecordedAlertEvent
                                            .Type
                                            .REINFORCED_REMINDER_SENT,
                                        now.plusSeconds(300),
                                    ),
                                com.vitahealth.tata.monitoring.application.readmodels
                                    .RecordedAlertEvent(
                                        com.vitahealth.tata.monitoring.application.readmodels
                                            .RecordedAlertEvent
                                            .Type
                                            .ALERT_GENERATED,
                                        now.plusSeconds(600),
                                    ),
                            ),
                        onOpenProfile = { opened++ },
                        contact =
                            com.vitahealth.tata.monitoring.presentation.alerts.viewmodel
                                .ContactUiState
                                .Ready(
                                    com.vitahealth.tata.monitoring.application.readmodels
                                        .ContactOption(
                                            com.vitahealth.tata.monitoring.domain.model.valueobjects
                                                .ContactChannel(
                                                    com.vitahealth.tata.monitoring.domain.model
                                                        .valueobjects
                                                        .ContactChannelType
                                                        .PHONE,
                                                    "+51999888777",
                                                ),
                                            "Rosa",
                                        )
                                ),
                    )
                }
            }
        }
        compose.onNodeWithText("Recordatorio reforzado enviado").assertIsDisplayed()
        compose.onNodeWithText("2 tabletas").assertIsDisplayed()
        compose.onNodeWithText("Rosa Vargas").assertIsDisplayed()
        compose.onNodeWithText("68 a\u00f1os").assertIsDisplayed()
        compose.onNodeWithText("Ver resumen").performClick()
        compose.runOnIdle { org.junit.Assert.assertEquals(1, opened) }
        compose.onNodeWithText("Contactar a Rosa").assertIsDisplayed()
        compose.onNodeWithText("Agregar nota de seguimiento").assertIsDisplayed()
        capture("alert-detail-profile", composeOnly = true)
    }

    @Test
    fun captureAlertsAndDetail() {
        val now = java.time.Instant.now()
        val alert =
            com.vitahealth.tata.monitoring.domain.model.entities.CaregiverAlert(
                1,
                "dose-test",
                "Losartán 50 mg",
                now,
                "Toma pendiente",
                com.vitahealth.tata.monitoring.domain.model.valueobjects.AlertStatus.OPEN,
                now,
                null,
            )
        var detail by mutableStateOf(false)
        var attended by mutableStateOf(false)
        compose.setContent {
            AuditTheme {
                Box(Modifier.safeDrawingPadding()) {
                    if (detail)
                        com.vitahealth.tata.monitoring.presentation.alerts.screens
                            .AlertDetailScreen(
                                com.vitahealth.tata.monitoring.presentation.alerts.viewmodel
                                    .AlertDetailUiState
                                    .Content(
                                        if (attended)
                                            alert.copy(
                                                status =
                                                    com.vitahealth.tata.monitoring.domain.model
                                                        .valueobjects
                                                        .AlertStatus
                                                        .ATTENDED
                                            )
                                        else alert
                                    )
                            )
                    else
                        com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertsScreen(
                            com.vitahealth.tata.monitoring.presentation.alerts.viewmodel
                                .AlertsUiState
                                .Content(listOf(alert))
                        )
                }
            }
        }
        capture("alerts-list", composeOnly = true)
        compose.runOnIdle { detail = true }
        capture("alert-detail")
        compose.runOnIdle { attended = true }
        capture("alert-attended-read-only")
    }

    @Composable
    private fun AuditTheme(
        preferences: AccessibilityPreferences = AccessibilityPreferences.Defaults,
        content: @Composable () -> Unit,
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        DisposableEffect(context) {
            var owner: android.content.Context = context
            while (
                owner is android.content.ContextWrapper && owner !is android.app.Activity
            ) owner = owner.baseContext
            (owner as? android.app.Activity)?.let { activity ->
                androidx.core.view.WindowCompat.getInsetsController(
                        activity.window,
                        activity.window.decorView,
                    )
                    .apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
            }
            onDispose {}
        }
        val configuration =
            android.content.res
                .Configuration(androidx.compose.ui.platform.LocalConfiguration.current)
                .apply { setLocales(android.os.LocaleList.forLanguageTags("es-419")) }
        val localized = remember(context) { context.createConfigurationContext(configuration) }
        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalContext provides localized,
            androidx.compose.ui.platform.LocalConfiguration provides configuration,
        ) {
            TataTheme(
                com.vitahealth.tata.shared.presentation.designsystem.accessibility
                    .TataAccessibility(
                        fontScale = preferences.textSize.scaleFactor,
                        highContrast = preferences.highContrast,
                        darkTheme = preferences.darkTheme,
                        reducedMotion = preferences.reducedMotion,
                        readingAssistance = preferences.readingAssistance,
                    ),
                content,
            )
        }
    }

    private fun capture(name: String, composeOnly: Boolean = false, dialog: Boolean = false) {
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        // Capture the rendered window, including system insets and shared navigation.
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(
            400
        ) // Give the emulator render thread time to present the settled Compose frame.
        val bitmap =
            if (composeOnly)
                (if (dialog) compose.onNode(isDialog()) else compose.onRoot())
                    .captureToImage()
                    .asAndroidBitmap()
            else
                requireNotNull(
                    InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                )
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val values =
            android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "audit-$name.png")
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
            }
        val uri =
            requireNotNull(
                resolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values,
                )
            )
        resolver.openOutputStream(uri)!!.use {
            check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
