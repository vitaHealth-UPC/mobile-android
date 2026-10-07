package com.vitahealth.tata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
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
        compose.setContent { AuditTheme { AdherenceHistoryScreen(state, Modifier.safeDrawingPadding(), selectedPeriod = period) } }
        capture("history-content")
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
        compose.setContent { AuditTheme { CareLinkScreen(state, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("link-code")
        compose.runOnIdle { state = state.copy(errorMessage = "El código no es válido o expiró.") }
        capture("link-invalid")
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
        compose.setContent { AuditTheme { MedicationRegistrationScreen(state, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("medication-registration")
        compose.runOnIdle { state = state.copy(name = "", presentation = "", errorMessage = "Completa los campos requeridos.") }
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
        var state by mutableStateOf(SessionAccessUiState())
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
        var state by mutableStateOf(CaregiverRegistrationUiState(name = "Diego Vargas", email = "diego@example.test"))
        compose.setContent { AuditTheme { CaregiverRegistrationScreen(state, {}, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
        capture("registration-content")
        compose.runOnIdle { state = state.copy(errorCode = "DUPLICATE_EMAIL", errorMessage = "Este correo ya está registrado.") }
        capture("registration-duplicate")
        compose.runOnIdle { state = state.copy(accountId = "account-test", step = RegistrationStep.VerificationExpired,
            errorCode = null, errorMessage = null) }
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

    @Test fun captureInventoryVariants() {
        val now = java.time.Instant.now()
        val stock = InventoryStockReadModel("med-test", 5, 7, StockStatus.LOW,
            listOf(InventoryBatch("batch-test", 30, now, "Lote 2026-09")), now, now, 5)
        var state by mutableStateOf(InventoryUiState.Ready(stock, "Losartán 50 mg", "comprimidos",
            replenishmentInput = "30", lotInput = "Lote 2026-09"))
        compose.setContent { AuditTheme { InventoryScreen(state, {}, {}, {}, {}, {}, {}, Modifier.safeDrawingPadding()) } }
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

    private fun capture(name: String) {
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
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
