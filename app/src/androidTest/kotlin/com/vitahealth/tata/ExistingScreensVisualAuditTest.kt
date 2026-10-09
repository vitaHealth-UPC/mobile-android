package com.vitahealth.tata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.analytics.presentation.history.*
import com.vitahealth.tata.analytics.presentation.recommendations.*
import com.vitahealth.tata.analytics.application.readmodels.*
import com.vitahealth.tata.carelink.presentation.link.*
import com.vitahealth.tata.preferences.presentation.notifications.*
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.treatment.presentation.medication.*
import com.vitahealth.tata.treatment.presentation.treatment.*
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus
import com.vitahealth.tata.monitoring.presentation.summary.*
import com.vitahealth.tata.monitoring.domain.model.*
import com.vitahealth.tata.identity.presentation.access.*
import com.vitahealth.tata.identity.presentation.registration.*
import com.vitahealth.tata.preferences.presentation.accessibility.*
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.carelink.presentation.profiles.*
import com.vitahealth.tata.carelink.application.LinkedAdult
import com.vitahealth.tata.inventory.presentation.inventory.*
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated presentation fixtures. No authentication or production requests. */
@RunWith(AndroidJUnit4::class)
class ExistingScreensVisualAuditTest {
    @get:Rule val compose = createComposeRule()

    @Test fun captureVoiceStates() {
        val now=java.time.Instant.now()
        val dose=com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel("voice","t","med","adult","Losartán cincuenta miligramos","1 comprimido","",now,com.vitahealth.tata.intake.domain.model.DoseStatus.PENDING)
        var phase by mutableStateOf(com.vitahealth.tata.intake.presentation.voice.VoicePhase.RECORDING)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            com.vitahealth.tata.intake.presentation.voice.VoiceConfirmationScreen(com.vitahealth.tata.intake.presentation.voice.VoiceConfirmationUiState(phase,dose),{},{},{})
        } } }
        compose.onNodeWithText("Escuchando...").assertExists()
        capture("voice-listening",composeOnly=true)
        compose.runOnIdle { phase=com.vitahealth.tata.intake.presentation.voice.VoicePhase.NOT_RECOGNIZED }
        compose.onNodeWithText("No reconocida").assertExists()
        compose.onNodeWithText("Confirmación no registrada").assertExists()
        capture("voice-not-recognized",composeOnly=true)
    }

    @Test fun captureLateDoseConfirmed() {
        val now=java.time.Instant.now()
        val dose=com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel("late","t","med","adult","Losartán 50 mg","1 comprimido","",now.minusSeconds(300),
            com.vitahealth.tata.intake.domain.model.DoseStatus.LATE,now)
        val next=com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel("next","t2","med2","adult","Metformina 850 mg","1 comprimido","",now.plusSeconds(3600),com.vitahealth.tata.intake.domain.model.DoseStatus.PENDING)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            com.vitahealth.tata.intake.presentation.detail.DoseDetailScreen(
                com.vitahealth.tata.intake.presentation.detail.DoseDetailUiState.Content(dose,nextDose=next,confirmationSucceeded=true,
                    outcome=com.vitahealth.tata.intake.presentation.detail.ConfirmationOutcome.LATE),{},{},{})
        } } }
        compose.onNodeWithText("Toma confirmada con retraso").assertExists()
        compose.onNodeWithText("Clasificada como tardía").assertExists()
        compose.onNodeWithText("Metformina 850 mg").assertExists()
        capture("late-dose-confirmed",composeOnly=true)
    }

    @Test fun captureDoseAlreadyConfirmed() {
        val now=java.time.Instant.now()
        val dose=com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel("done","t","med","adult","Losartán 50 mg","1 comprimido","",now,
            com.vitahealth.tata.intake.domain.model.DoseStatus.CONFIRMED,now,alreadyConfirmed=true)
        val next=com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel("next","t2","med2","adult","Metformina 850 mg","1 comprimido","",now.plusSeconds(3600),com.vitahealth.tata.intake.domain.model.DoseStatus.PENDING)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            com.vitahealth.tata.intake.presentation.detail.DoseConfirmedScreen(
                com.vitahealth.tata.intake.presentation.detail.DoseDetailUiState.Content(dose,nextDose=next,confirmationSucceeded=true,
                    outcome=com.vitahealth.tata.intake.presentation.detail.ConfirmationOutcome.ALREADY_CONFIRMED),{})
        } } }
        compose.onNodeWithText("Toma ya confirmada").assertExists()
        compose.onNodeWithText("Sin duplicados").assertExists()
        capture("dose-already-confirmed",composeOnly=true)
    }

    @Test fun captureDoseConfirmed() {
        val now=java.time.Instant.now()
        val dose=com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel("done","t","med","adult","Losartán 50 mg","1 comprimido","",now,
            com.vitahealth.tata.intake.domain.model.DoseStatus.CONFIRMED,now)
        val next=com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel("next","t2","med2","adult","Metformina 850 mg","1 comprimido","",now.plusSeconds(3600),com.vitahealth.tata.intake.domain.model.DoseStatus.PENDING)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            com.vitahealth.tata.intake.presentation.detail.DoseConfirmedScreen(
                com.vitahealth.tata.intake.presentation.detail.DoseDetailUiState.Content(dose,nextDose=next,confirmationSucceeded=true),{})
        } } }
        compose.onNodeWithText("¡Bien hecho!").assertExists()
        capture("dose-confirmed",composeOnly=true)
    }

    @Test fun capturePersonalNotesAndFilter() {
        val zone=java.time.ZoneId.systemDefault()
        val today=java.time.LocalDate.now()
        fun date(days:Long,hour:Int,minute:Int)=today.minusDays(days).atTime(hour,minute).atZone(zone).toInstant()
        val notes=listOf(
            PersonalNote(1,"Tomar con agua","Losartán 50 mg. Mantener el mismo horario y preparar un vaso de agua antes de la toma.",PersonalNoteCategory.MEDICATION,date(0,8,5)),
            PersonalNote(2,"Antes de salir","Revisar las próximas tomas si voy a estar fuera de casa.",PersonalNoteCategory.ROUTINE,date(1,17,40)),
            PersonalNote(3,"Rutina de la noche","Dejar el medicamento junto al vaso de agua.",PersonalNoteCategory.ROUTINE,date(2,21,10)),
        )
        var category by mutableStateOf<PersonalNoteCategory?>(null)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            com.vitahealth.tata.monitoring.presentation.personalnotes.PersonalNotesScreen(
                com.vitahealth.tata.monitoring.presentation.personalnotes.PersonalNotesUiState(notes=notes,loading=false,category=category),
                {},{category=it},{},{},{})
        } } }
        capture("personal-notes",composeOnly=true)
        compose.onNodeWithText("Rutina").performClick()
        compose.onNodeWithText("Antes de salir").assertExists()
        capture("personal-notes-routine",composeOnly=true)
    }

    @Test fun captureMyMedicationsAndHistory() {
        val medications = listOf(
            com.vitahealth.tata.treatment.application.readmodels.MyMedication("1", "Losartán 50 mg", "Comprimido", true, "1 comprimido", "Cada mañana", listOf("08:00")),
            com.vitahealth.tata.treatment.application.readmodels.MyMedication("2", "Metformina\n850 mg", "Comprimido", true, "1 comprimido", "Con el desayuno", listOf("08:00")),
            com.vitahealth.tata.treatment.application.readmodels.MyMedication("3", "Atorvastatina\n20 mg", "Comprimido", true, "1 comprimido", "Por la noche", listOf("20:00")),
            com.vitahealth.tata.treatment.application.readmodels.MyMedication("4", "Vitamina D3\n1,000 UI", "Cápsula", true, "1 cápsula", "Cada mañana", listOf("08:00")),
            com.vitahealth.tata.treatment.application.readmodels.MyMedication("5", "Amlodipino 5 mg", "Comprimido", true, "1 comprimido", "Cada noche", listOf("20:00")),
        )
        var history by mutableStateOf(false)
        val state = MyMedicationsUiState(medications = medications, loading = false,
            nextDose = com.vitahealth.tata.treatment.application.readmodels.MedicationNextDose("intake", "1", java.time.Instant.parse("2026-10-08T13:00:00Z")))
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            MyMedicationsScreen(state.copy(history = history), {}, { history = it }, {}, {}, {})
        } } }
        capture("my-medications", composeOnly = true)
        compose.onNodeWithText("Historial").performClick()
        compose.onNodeWithText("No hay medicamentos en el historial.").assertExists()
        capture("my-medications-history-empty", composeOnly = true)
    }

    @Test fun defaultResourcesUseSpanishAndEnglishIsExplicit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        fun localized(tag: String): android.content.Context {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocales(android.os.LocaleList.forLanguageTags(tag))
            return context.createConfigurationContext(config)
        }
        val spanish = localized("fr-FR") // An unsupported device language must use the Spanish fallback.
        check(spanish.getString(com.vitahealth.tata.R.string.onboarding_start) == "Comenzar")
        check(spanish.getString(com.vitahealth.tata.R.string.alerts_title) == "Alertas")
        check(spanish.getString(com.vitahealth.tata.R.string.inventory_title) == "Inventario")
        check(spanish.getString(com.vitahealth.tata.R.string.accessibility_title) == "Accesibilidad")
        val english = localized("en-US")
        check(english.getString(com.vitahealth.tata.R.string.onboarding_start) == "Get started")
        check(english.getString(com.vitahealth.tata.R.string.alerts_title) == "Alerts")
    }

    @Test fun captureOnboardingAndPlanVariants() {
        val essential = com.vitahealth.tata.identity.domain.model.Plan("ESSENTIAL", "Esencial",
            java.math.BigDecimal("9.90"), "PEN", setOf(com.vitahealth.tata.identity.domain.model.PlanCapability.REMINDERS))
        val family = essential.copy(code = "FAMILY", name = "Familiar", monthlyPrice = java.math.BigDecimal("19.90"),
            capabilities = com.vitahealth.tata.identity.domain.model.PlanCapability.entries.toSet())
        val state = com.vitahealth.tata.identity.presentation.subscription.PlanSubscriptionUiState(
            isLoading = false, plans = listOf(essential, family), subscription =
                com.vitahealth.tata.identity.domain.model.Subscription("account-test", family,
                    com.vitahealth.tata.identity.domain.model.SubscriptionStatus.ACTIVE,
                    java.time.Instant.parse("2026-10-28T00:00:00Z")))
        var variant by mutableStateOf(0)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            if (variant == 0) com.vitahealth.tata.identity.presentation.onboarding.OnboardingScreen({}, {})
            else com.vitahealth.tata.identity.presentation.subscription.PlanSubscriptionScreen(
                state.copy(changeMessage = if (variant == 2)
                    com.vitahealth.tata.identity.presentation.subscription.PlanChangeMessage.Updated else null),
                {}, {}, {}, {}, {}, {})
        } } }
        capture("onboarding")
        compose.runOnIdle { variant = 1 }
        capture("plan-subscription")
        compose.runOnIdle { variant = 2 }
        capture("plan-updated")
    }

    @Test fun captureHistoryVariants() {
        val summary = AdherenceSummaryUi("Últimos 30 días", 92, "+4%", 87, "+3%", 2, 1,
            "2 tardías · 1 omitida", listOf(AdherenceTrendPoint("S1", 80), AdherenceTrendPoint("S2", 87),
                AdherenceTrendPoint("S3", 90), AdherenceTrendPoint("S4", 92)),
            recentIntakes = listOf(RecentIntakeUi("Hoy, 8:00 a. m.", "Losartán", "A tiempo", com.vitahealth.tata.analytics.domain.model.IntakeOutcomeStatus.OnTime)),
            pattern = PatternUi("Excelente progreso", "Los retrasos se concentran por la tarde."))
        var state by mutableStateOf<AdherenceHistoryUiState>(AdherenceHistoryUiState.Content(summary))
        var period by mutableStateOf(com.vitahealth.tata.analytics.domain.model.AdherencePeriod.LastMonth)
        compose.setContent { AuditTheme { AdherenceHistoryScreen(state, Modifier.safeDrawingPadding(), selectedPeriod = period,
            onPeriodSelected = { period = it }) } }
        capture("history-content")
        compose.onNodeWithText("Últimos 30 días âŒ„").performClick()
        compose.onNodeWithText("Últimos 7 días").performClick()
        compose.runOnIdle { check(period == com.vitahealth.tata.analytics.domain.model.AdherencePeriod.LastWeek)
            period = com.vitahealth.tata.analytics.domain.model.AdherencePeriod.LastMonth }
        compose.runOnIdle { state = AdherenceHistoryUiState.InsufficientData("Últimos 30 días") }
        capture("history-insufficient")
        compose.runOnIdle { state = AdherenceHistoryUiState.NoResults("Últimos 30 días") }
        capture("history-empty")
        compose.runOnIdle { period = com.vitahealth.tata.analytics.domain.model.AdherencePeriod.LastWeek
            state = AdherenceHistoryUiState.Content(summary.copy(periodLabel = "Últimos 7 días"), periodUpdated = true) }
        capture("history-period-changed")
    }

    @Test fun captureRecommendationsVariants() {
        val data = AdherenceRecommendationsReadModel(30, "Tomas de la noche", "Los retrasos se concentran por la noche.",
            List(7) { List(4) { 0.4f } }, listOf(RecommendationReadModel("Ajusta el recordatorio", "Programa un aviso antes de tu toma.")))
        var state by mutableStateOf<AdherenceRecommendationsUiState>(AdherenceRecommendationsUiState.Content(data))
        compose.setContent { AuditTheme { AdherenceRecommendationsScreen(state, Modifier.safeDrawingPadding()) } }
        capture("recommendations-content")
        compose.runOnIdle { state = AdherenceRecommendationsUiState.InsufficientEvidence }
        capture("recommendations-insufficient")
    }

    @Test fun captureLinkVariants() {
        var state by mutableStateOf(CareLinkUiState("caregiver-test", code = "TATA-4821",
            olderAdult = com.vitahealth.tata.carelink.domain.model.OlderAdultProfile("adult-test", "Rosa Vargas",
                java.time.LocalDate.of(1958, 5, 12), null, null, null)))
        compose.setContent { AuditTheme { CareLinkScreen(state, {}, {}, {}, {}, Modifier.safeDrawingPadding(),
            avatarResource = com.vitahealth.tata.R.drawable.figma_link_avatar) } }
        capture("link-code")
        compose.runOnIdle { state = state.copy(code = "INVALIDO", errorMessage = "El código ya venció o fue utilizado.") }
        capture("link-invalid")
    }

    @Test fun captureLogin() {
        var requested = false
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            SessionAccessScreen(SessionAccessUiState(email = "diego@example.test", password = "fixture-only"),
                {}, {}, { requested = true }, {}, {}, showPin = false)
        } } }
        compose.onNodeWithText("Iniciar sesión").performClick()
        compose.runOnIdle { check(requested) }
        capture("login")
    }

    @Test fun captureNotificationVariants() {
        var state by mutableStateOf(NotificationPreferencesUiState(isLoading = false, preferences = NotificationPreferences(
            quietHours = com.vitahealth.tata.preferences.domain.model.QuietHours.Default)))
        compose.setContent { AuditTheme { NotificationPreferencesScreen(state, {}, {}, {}, { _, _ -> }, { _, _ -> },
            { _, _ -> }, Modifier.safeDrawingPadding()) } }
        capture("notifications-content")
        compose.runOnIdle { state = state.copy(message = NotificationMessage.Saved) }
        capture("notifications-saved")
    }

    @Test fun captureMedicationRegistrationVariants() {
        var state by mutableStateOf(MedicationRegistrationUiState("caregiver-test", "adult-test", "Rosa Vargas",
            name = "Losartán 50 mg", presentation = "Comprimido", frequency = "Una vez al día", timing = "Con el desayuno"))
        compose.setContent { AuditTheme { MedicationRegistrationScreen(state, {}, {}, { state = state.copy(frequency = it) }, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        compose.onNodeWithText("Una vez al día").performClick()
        compose.onNodeWithText("Cada 12 horas").performClick()
        compose.runOnIdle { check(state.frequency == "Cada 12 horas") }
        capture("medication-registration")
        compose.runOnIdle { state = state.copy(name = "", presentation = "", frequency = "", timing = "", errorMessage = "Completa medicamento, dosis y frecuencia antes de continuar.") }
        capture("medication-registration-error")
    }

    @Test fun captureTreatmentCreationVariants() {
        var state by mutableStateOf(TreatmentCreationUiState("caregiver-test", "adult-test", "Rosa Vargas", "med-test", "Losartán 50 mg"))
        compose.setContent { AuditTheme { TreatmentCreationScreen(state, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("treatment-create")
        compose.runOnIdle { state = state.copy(errorMessage = "Completa el tratamiento antes de continuar.") }
        capture("treatment-incomplete")
    }

    @Test fun captureMedicationManagementVariants() {
        val medication = Medication("med-test", "adult-test", "Losartán 50 mg", "Comprimido", true)
        var state by mutableStateOf(MedicationManagementUiState("Rosa Vargas", isLoading = false, medications = listOf(medication)))
        compose.setContent { AuditTheme { MedicationManagementScreen(state, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("medication-management")
        compose.runOnIdle { state = state.copy(message = MedicationManagementMessage.Updated) }
        capture("medication-updated")
        compose.runOnIdle { state = state.copy(message = MedicationManagementMessage.Deactivated, medications = listOf(medication.copy(active = false))) }
        capture("medication-deactivated")
        compose.runOnIdle { state = state.copy(accessDenied = true, medications = emptyList(), message = null) }
        capture("treatment-access-denied")
    }

    @Test fun captureTreatmentPaused() {
        val state = TreatmentLifecycleUiState("caregiver-test", "adult-test", "Rosa Vargas", "med-test", "Losartán 50 mg", "treatment-test",
            "Control de presión", "1 comprimido", "Cada día", "8:00 a. m.", "Con el desayuno", 15, status = TreatmentStatus.PAUSED)
        compose.setContent { AuditTheme { TreatmentLifecycleScreen(state, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("treatment-paused")
    }

    @Test fun captureFamilySummaryVariants() {
        val summary = FamilySummary("Rosa Vargas", java.time.LocalDate.now(), AdherenceCounts(12, 14), AdherenceCounts(2, 3),
            MonitoredDose("dose-test", "med-test", "Losartán 50 mg", "1 tableta", java.time.Instant.now().plusSeconds(900), "PENDING"),
            listOf(StockAttention("med-test", "Losartán 50 mg", 5)),
            listOf(OpenAlert(1, "Losartán 50 mg", "Toma pendiente", java.time.Instant.now())), true)
        var state by mutableStateOf(FamilySummaryUiState(loading = false, summary = summary))
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            FamilySummaryScreen(state, "Rosa Vargas", {}, {}, {}, {}, {}, {}, {}, {})
        } } }
        capture("family-summary")
        compose.runOnIdle { state = FamilySummaryUiState(loading = false, error = "Necesitas consentimiento del adulto mayor.") }
        capture("family-consent-required")
    }

    @Test fun capturePinVariants() {
        var state by mutableStateOf(SessionAccessUiState(pin = "4821"))
        var setup by mutableStateOf(false)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            PinAccessScreen(state, "Rosa Vargas", setup, {}, {}, {})
        } } }
        capture("pin-access")
        compose.runOnIdle { setup = true }
        capture("pin-setup")
        compose.runOnIdle { setup = false; state = state.copy(error = "INVALID_PIN") }
        capture("pin-incorrect")
        compose.runOnIdle { state = state.copy(error = "PIN_LOCKED") }
        capture("pin-blocked")
    }

    @Test fun captureRegistrationVariants() {
        var state by mutableStateOf(CaregiverRegistrationUiState(name = "Diego Vargas", email = "diego@example.test",
            password = "Fixture4821"))
        compose.setContent { AuditTheme { CaregiverRegistrationScreen(state, {}, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("registration-content")
        compose.runOnIdle { state = state.copy(accountId = "account-test", step = RegistrationStep.Verification,
            verificationCode = "482196") }
        capture("registration-verification")
        compose.runOnIdle { state = state.copy(accountId = null, step = RegistrationStep.Account,
            verificationCode = "", errorCode = "DUPLICATE_EMAIL", errorMessage = "Este correo ya está registrado.") }
        capture("registration-duplicate")
        compose.runOnIdle { state = state.copy(accountId = "account-test", step = RegistrationStep.VerificationExpired,
            verificationCode = "482196", errorCode = null, errorMessage = null) }
        capture("registration-expired")
    }

    @Test fun captureAccessibilityVariants() {
        var state by mutableStateOf(AccessibilityUiState())
        compose.setContent { AuditTheme(state.preferences) { AccessibilityScreen(state, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("accessibility-content")
        val variants = listOf(
            Triple("large-text", AccessibilityPreferences(textSize = TextSizeLevel.LARGE), AccessibilityMessage.LargeTextSaved),
            Triple("high-contrast", AccessibilityPreferences(highContrast = true), AccessibilityMessage.HighContrastSaved),
            Triple("reduced-motion", AccessibilityPreferences(reducedMotion = true), AccessibilityMessage.ReducedMotionSaved),
            Triple("reading-assistance", AccessibilityPreferences(readingAssistance = true), AccessibilityMessage.ReadingAssistanceSaved))
        for ((name, preferences, message) in variants) {
            compose.runOnIdle { state = AccessibilityUiState(preferences = preferences, message = message) }
            capture("accessibility-$name")
        }
    }

    @Test fun captureLinkedPerson() {
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            CaregiverProfilesScreen(CaregiverProfilesState(adults = listOf(LinkedAdult("adult-test", "Rosa Vargas"))),
                {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
        } } }
        capture("linked-person")
    }

    @Test fun captureConsentRequired() {
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            FamilySummaryScreen(FamilySummaryUiState(loading = false,
                error = "El vínculo de cuidado ya no está activo.", errorCode = "CARE_RELATIONSHIP_REQUIRED"),
                "Rosa Vargas", {}, {}, {}, {}, {}, {}, {}, {})
        } } }
        compose.onNodeWithText("Seguimiento restringido").assertExists()
        compose.onNodeWithText("Adherencia esta semana").assertDoesNotExist()
        capture("consent-required")
    }

    @Test fun captureInventoryVariants() {
        val now = java.time.Instant.now()
        val stock = InventoryStockReadModel("med-test", 5, 7, StockStatus.LOW,
            listOf(InventoryBatch("batch-test", 30, now, "Lote 2026-09")), now, now, 5)
        var state by mutableStateOf(InventoryUiState.Ready(stock, "Losartán 50 mg", "comprimidos",
            replenishmentInput = "30", lotInput = "Lote 2026-09"))
        compose.setContent { AuditTheme { InventoryScreen(state, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding(), olderAdultName = "Rosa Vargas") } }
        capture("inventory-content")
        compose.runOnIdle { state = state.copy(replenishmentInput = "0", replenishmentErrorCode = "INVALID_QUANTITY") }
        capture("inventory-invalid")
        compose.runOnIdle { state = state.copy(stock = stock.copy(remainingStock = 35, daysRemaining = 35, status = StockStatus.AVAILABLE),
            replenishmentInput = "30", replenishmentErrorCode = null, justRegistered = true) }
        capture("inventory-replenished")
    }

    @Test fun captureTreatmentList() {
        val repository = object : com.vitahealth.tata.treatment.application.TreatmentCatalogRepository {
            override suspend fun listTreatments(caregiverId: String, olderAdultId: String) =
                com.vitahealth.tata.shared.common.result.AppResult.Success(listOf(
                    com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel("treatment-test", "adult-test",
                        "Control de presión", TreatmentStatus.ACTIVE, "med-test", "1 comprimido", "Cada día",
                        listOf("08:00"), "Con agua", 10)))
        }
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            TreatmentListRoute(TreatmentListViewModel.Factory("caregiver-test", "adult-test",
                com.vitahealth.tata.treatment.application.handlers.ListTreatmentsQueryHandler(repository)), "Rosa Vargas", {}, {}, {})
        } } }
        compose.waitUntil(5_000) { compose.onAllNodes(androidx.compose.ui.test.hasText("Control de presión")).fetchSemanticsNodes().isNotEmpty() }
        capture("treatment-list")
    }

    @Test fun captureCaregiverNotesVariants() {
        val note = FollowUpNote(1, "Prefiere recibir una llamada si no responde al segundo recordatorio.", java.time.Instant.now(), "caregiver-test")
        var saved by mutableStateOf(false)
        var showAlert by mutableStateOf(false)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            if (showAlert) com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailScreen(
                com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailUiState.Content(
                    CaregiverAlert(1, "dose-test", "Losartán 50 mg", note.recordedAt, "Toma pendiente", AlertStatus.OPEN, note.recordedAt, null)),
                noteSaved = true)
            else com.vitahealth.tata.monitoring.presentation.notes.NotesScreen(
                com.vitahealth.tata.monitoring.presentation.notes.NotesUiState.Content(listOf(note)),
                caregiverId = "caregiver-test", noteSaved = saved)
        } } }
        capture("caregiver-notes")
        compose.runOnIdle { saved = true }
        capture("caregiver-notes-saved")
        compose.runOnIdle { showAlert = true }
        capture("alert-followup-note-saved")
    }

    @Test fun captureAlertsAndDetail() {
        val now = java.time.Instant.now()
        val alert = com.vitahealth.tata.monitoring.domain.model.CaregiverAlert(1, "dose-test", "Losartán 50 mg", now,
            "Toma pendiente", com.vitahealth.tata.monitoring.domain.model.AlertStatus.OPEN, now, null)
        var detail by mutableStateOf(false)
        var attended by mutableStateOf(false)
        compose.setContent { AuditTheme { Box(Modifier.safeDrawingPadding()) {
            if (detail) com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailScreen(
                com.vitahealth.tata.monitoring.presentation.alerts.AlertDetailUiState.Content(
                    if (attended) alert.copy(status = com.vitahealth.tata.monitoring.domain.model.AlertStatus.ATTENDED) else alert))
            else com.vitahealth.tata.monitoring.presentation.alerts.AlertsScreen(
                com.vitahealth.tata.monitoring.presentation.alerts.AlertsUiState.Content(listOf(alert)))
        } } }
        capture("alerts-list")
        compose.runOnIdle { detail = true }
        capture("alert-detail")
        compose.runOnIdle { attended = true }
        capture("alert-attended-read-only")
    }

    @Composable private fun AuditTheme(preferences: AccessibilityPreferences = AccessibilityPreferences.Defaults, content: @Composable () -> Unit) {
        val context = androidx.compose.ui.platform.LocalContext.current
        DisposableEffect(context) {
            var owner: android.content.Context = context
            while (owner is android.content.ContextWrapper && owner !is android.app.Activity) owner = owner.baseContext
            (owner as? android.app.Activity)?.let { activity ->
                androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
            onDispose {}
        }
        val configuration = android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current).apply {
            setLocales(android.os.LocaleList.forLanguageTags("es-419"))
        }
        val localized = remember(context) { context.createConfigurationContext(configuration) }
        CompositionLocalProvider(androidx.compose.ui.platform.LocalContext provides localized,
            androidx.compose.ui.platform.LocalConfiguration provides configuration) {
            TataTheme(com.vitahealth.tata.shared.design.accessibility.TataAccessibility(
                fontScale = preferences.textSize.scaleFactor, highContrast = preferences.highContrast,
                reducedMotion = preferences.reducedMotion, readingAssistance = preferences.readingAssistance), content)
        }
    }

    private fun capture(name: String, composeOnly: Boolean = false) {
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        // Capture the rendered window, including system insets and shared navigation.
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(400) // Give the emulator render thread time to present the settled Compose frame.
        val bitmap = if (composeOnly) compose.onRoot().captureToImage().asAndroidBitmap()
            else requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "audit-$name.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
        }
        val uri = requireNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
        resolver.openOutputStream(uri)!!.use { check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)) }
    }
}
