package com.vitahealth.tata.treatment

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.graphs.openTreatmentManagement
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.application.ports.TreatmentDetailRepository
import com.vitahealth.tata.treatment.application.queries.GetTreatmentDetailQueryHandler
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentDetailRoute
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleRoute
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentDetailViewModel
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Exercises real feature routes, handlers and the host URL mapper with a controlled server port.
 */
class TreatmentManagementNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun existingTreatmentCanPauseReturnRefreshAndResumeWithoutActivation() {
        var serverStatus = TreatmentStatus.ACTIVE
        var serverDose = "1 tableta"
        var reads = 0
        var denied = false
        var selectedTab: CaregiverTab? = null
        var medicationManagementRequests = 0
        val mutations = mutableListOf<String>()
        val reader =
            object : TreatmentDetailRepository {
                override suspend fun getTreatmentDetail(
                    caregiverId: String,
                    treatmentId: String,
                ): AppResult<TreatmentDetailReadModel> {
                    assertEquals("caregiver", caregiverId)
                    assertEquals("treatment", treatmentId)
                    reads++
                    if (denied)
                        return AppResult.Failure("Revoked", code = "CARE_LINK_NOT_AUTHORIZED")
                    return AppResult.Success(
                        TreatmentDetailReadModel(
                            "treatment",
                            "adult",
                            "Control / presión",
                            serverStatus,
                            "medication",
                            serverDose,
                            "Diaria",
                            listOf("08:00"),
                            "Con agua",
                            7,
                            10,
                        )
                    )
                }
            }
        val repository =
            object : TreatmentLifecycleRepository {
                override suspend fun configureTreatment(
                    caregiverId: String,
                    treatmentId: String,
                    medicationId: String,
                    dose: String,
                    frequency: String,
                    scheduledTimes: List<String>,
                    instructions: String,
                    reminderLeadMinutes: Int,
                    followUpDelayMinutes: Int?,
                ): AppResult<Treatment> {
                    assertEquals("caregiver", caregiverId)
                    assertEquals("treatment", treatmentId)
                    assertEquals("medication", medicationId)
                    assertEquals(7, reminderLeadMinutes)
                    assertEquals(10, followUpDelayMinutes)
                    serverDose = dose
                    mutations += "configure"
                    return AppResult.Success(
                        Treatment("treatment", "adult", "Control", serverStatus)
                    )
                }

                override suspend fun activateTreatment(
                    caregiverId: String,
                    treatmentId: String,
                ): AppResult<Treatment> = error("Managing must not activate again")

                override suspend fun pauseTreatment(
                    caregiverId: String,
                    treatmentId: String,
                ): AppResult<Treatment> {
                    assertEquals("caregiver", caregiverId)
                    assertEquals("treatment", treatmentId)
                    if (denied)
                        return AppResult.Failure("Revoked", code = "CARE_LINK_NOT_AUTHORIZED")
                    mutations += "pause"
                    serverStatus = TreatmentStatus.PAUSED
                    return AppResult.Success(
                        Treatment("treatment", "adult", "Control", serverStatus)
                    )
                }

                override suspend fun resumeTreatment(
                    caregiverId: String,
                    treatmentId: String,
                ): AppResult<Treatment> {
                    assertEquals("caregiver", caregiverId)
                    assertEquals("treatment", treatmentId)
                    mutations += "resume"
                    serverStatus = TreatmentStatus.ACTIVE
                    return AppResult.Success(
                        Treatment("treatment", "adult", "Control", serverStatus)
                    )
                }
            }
        lateinit var nav: NavHostController
        compose.setContent {
            val context = LocalContext.current
            val spanish = remember(context) { context.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme {
                    nav = rememberNavController()
                    NavHost(nav, startDestination = "detail") {
                        composable("profiles") { Text("Personas vinculadas") }
                        composable("detail") {
                            TreatmentDetailRoute(
                                TreatmentDetailViewModel.Factory(
                                    "caregiver",
                                    "Rosa Vargas",
                                    "treatment",
                                    "Losartán 50 mg",
                                    GetTreatmentDetailQueryHandler(reader),
                                ),
                                onBack = {},
                                onRestrictedBack = {
                                    nav.navigate("profiles") {
                                        popUpTo("detail") { inclusive = true }
                                    }
                                },
                                navigationBar = {
                                    CaregiverTabBar(CaregiverTab.Person, { selectedTab = it })
                                },
                                onOpenInventory = { _, _ -> },
                                onManageTreatment = { detail, label ->
                                    nav.openTreatmentManagement(
                                        "caregiver",
                                        "Rosa Vargas",
                                        detail,
                                        label,
                                    )
                                },
                            )
                        }
                        composable(
                            RootDestination.TreatmentLifecycle.route,
                            arguments =
                                listOf(
                                    navArgument(
                                        RootDestination.TreatmentLifecycle.initialStatusArgument
                                    ) {
                                        type = NavType.StringType
                                        defaultValue = "DRAFT"
                                    }
                                ),
                        ) { entry ->
                            fun value(key: String) = requireNotNull(entry.arguments?.getString(key))
                            val d = RootDestination.TreatmentLifecycle
                            TreatmentLifecycleRoute(
                                TreatmentLifecycleViewModel.Factory(
                                    value(d.caregiverIdArgument),
                                    value(d.olderAdultIdArgument),
                                    value(d.olderAdultNameArgument),
                                    value(d.medicationIdArgument),
                                    value(d.medicationLabelArgument),
                                    value(d.treatmentIdArgument),
                                    value(d.treatmentNameArgument),
                                    value(d.dosageArgument),
                                    value(d.frequencyArgument),
                                    value(d.scheduleTextArgument),
                                    value(d.instructionsArgument),
                                    value(d.reminderDelayArgument).toInt(),
                                    ConfigureTreatmentCommandHandler(repository),
                                    ChangeTreatmentStatusCommandHandler(repository),
                                    currentTreatment = GetTreatmentDetailQueryHandler(reader),
                                    initialStatus =
                                        TreatmentStatus.valueOf(value(d.initialStatusArgument)),
                                    adherence =
                                        com.vitahealth.tata.treatment.application.ports
                                            .TreatmentAdherenceReader { adultId, treatmentId ->
                                                assertEquals("adult", adultId)
                                                assertEquals("treatment", treatmentId)
                                                AppResult.Success(
                                                    com.vitahealth.tata.treatment.application
                                                        .readmodels
                                                        .TreatmentAdherenceReadModel(
                                                            92,
                                                            listOf(82, 92, 74, 96, 88, 94, 92)
                                                                .mapIndexed { index, percent ->
                                                                    com.vitahealth.tata.treatment
                                                                        .application
                                                                        .readmodels
                                                                        .TreatmentAdherencePoint(
                                                                            java.time.LocalDate
                                                                                .now()
                                                                                .minusDays(
                                                                                    6L - index
                                                                                ),
                                                                            percent,
                                                                        )
                                                                },
                                                        )
                                                )
                                            },
                                ),
                                onOpenDetail = { nav.popBackStack() },
                                onManageMedication = { medicationManagementRequests++ },
                                navigationBar = { canNavigate ->
                                    CaregiverTabBar(
                                        CaregiverTab.Person,
                                        { if (canNavigate) selectedTab = it },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        compose.onNodeWithText("Gestionar pauta").performScrollTo().performClick()
        compose.onNodeWithText("Activar tratamiento").assertDoesNotExist()
        compose.onNodeWithText("Control / presión").assertExists()
        capture("treatment24-top")
        compose.onNodeWithText("Historial protegido").performScrollTo()
        compose
            .onNodeWithText("Adherencia de este tratamiento")
            .performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithText("92%").assertIsDisplayed()
        compose.onNodeWithText("Desactivar medicamento").performScrollTo()
        capture("treatment24-bottom")
        compose.onNodeWithText("Desactivar medicamento").performClick()
        compose.runOnIdle { assertEquals(1, medicationManagementRequests) }
        compose.onNodeWithText("Persona").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithText("Alertas").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(CaregiverTab.Alerts, selectedTab) }
        compose.onNodeWithText("Pausar").performScrollTo().performClick()
        compose.onNodeWithText("Reanudar").assertExists()
        compose.onNodeWithText("Tratamiento pausado").assertIsDisplayed()
        capture("treatment44-paused")
        Espresso.pressBack()
        compose.onNodeWithText("Pausado").assertExists()
        compose.onNodeWithText("Gestionar pauta").performScrollTo().performClick()
        compose.onNodeWithText("Activar tratamiento").assertDoesNotExist()
        compose.onNodeWithText("Reanudar").performScrollTo().performClick()
        Espresso.pressBack()
        compose.onNodeWithText("Activo").assertExists()
        compose.onNodeWithText("Gestionar pauta").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("pause", "resume"), mutations) }
        compose.onNodeWithText("Editar pauta").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("2 tabletas")
        compose.onNodeWithText("Guardar").performClick()
        compose.onNodeWithText("Cambios guardados").assertIsDisplayed()
        compose.waitUntil(5_000) { compose.onAllNodes(isDialog()).fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("2 tabletas, Diaria, 08:00").assertExists()
        capture("treatment37-updated")
        compose.runOnIdle { denied = true }
        compose.onNodeWithText("Pausar").performScrollTo().performClick()
        compose.onNodeWithText("Volver al detalle").assertExists()
        compose.onNodeWithText("Pausar").assertDoesNotExist()
        compose.onNodeWithText("Editar pauta").assertDoesNotExist()
        compose.onNodeWithText("Desactivar medicamento").assertDoesNotExist()
        compose.onNodeWithText("Losartán 50 mg").assertDoesNotExist()
        Espresso.pressBack()
        compose.onNodeWithText("Acceso restringido").assertExists()
        compose.onNodeWithText("Gestionar pauta").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals(listOf("pause", "resume", "configure"), mutations)
            assertEquals(1, medicationManagementRequests)
            assertTrue("Returning to detail must reload the acknowledged status", reads >= 3)
            assertEquals("detail", nav.currentDestination?.route)
        }
        compose.onNodeWithText("Persona").assertIsDisplayed().assertIsSelected()
        compose.onNodeWithText("Volver a Persona vinculada").assertIsDisplayed()
        capture("treatment45-restricted")
        compose.onNodeWithText("Volver a Persona vinculada").performClick()
        compose.onNodeWithText("Personas vinculadas").assertIsDisplayed()
        compose.runOnIdle { assertEquals("profiles", nav.currentDestination?.route) }
    }

    /** Device-backed evidence for the current Treatment #24 Figma review. */
    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        check(bitmap.width >= 100 && bitmap.height >= 100)
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri =
            requireNotNull(
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, name + ".png")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TataAudit")
                    },
                )
            )
        resolver.openOutputStream(uri)!!.use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }
}
