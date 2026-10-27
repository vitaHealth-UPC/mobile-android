package com.vitahealth.tata.treatment

import android.graphics.Bitmap
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.navigation.destinations.RootDestination
import com.vitahealth.tata.app.navigation.graphs.completeTreatmentConfiguration
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.application.commands.ChangeTreatmentStatusCommandHandler
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository
import com.vitahealth.tata.treatment.presentation.treatment.TreatmentLifecycleRoute
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleViewModel
import java.io.File
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** A rejected regimen returns to its exact saved draft without activating or posting a retry. */
class TreatmentIncompleteRecoveryTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun rejectedConfigurationShowsItsVariantAndPreservesTheExistingWizard() = runRecovery(true)

    @Test fun directReviewRecreatesOnlyItsOwnDraftWithTheOriginalInputs() = runRecovery(false)

    private fun runRecovery(existingWizard: Boolean) {
        val calls = mutableListOf<String>()
        val repository =
            Proxy.newProxyInstance(
                TreatmentLifecycleRepository::class.java.classLoader,
                arrayOf(TreatmentLifecycleRepository::class.java),
            ) { _, method, _ ->
                calls += method.name
                check(method.name == "configureTreatment") {
                    "Incomplete configuration must not activate"
                }
                AppResult.Failure("Completa la pauta", code = "REQUEST_VALIDATION_FAILED")
            } as TreatmentLifecycleRepository
        val root = RootDestination.TreatmentDoseFrequency
        val rootRoute =
            root.createRoute(
                "caregiver",
                "adult",
                "Rosa Vargas",
                "medication",
                "Losartán 50 mg",
                "treatment",
                "Control de presión",
            )
        val reviewRoute =
            RootDestination.TreatmentLifecycle.createRoute(
                "caregiver",
                "adult",
                "Rosa Vargas",
                "medication",
                "Losartán 50 mg",
                "treatment",
                "Control de presión",
                "2 tabletas",
                "Diaria",
                "08:15,20:15",
                "Con agua",
                10,
            )
        var recoveredSchedule: String? = null
        compose.setContent {
            TataTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = if (existingWizard) rootRoute else reviewRoute) {
                    composable(root.route) { entry ->
                        recoveredSchedule = entry.savedStateHandle.get<String>("scheduleText")
                        Text(
                            "Pauta conservada: " +
                                (entry.savedStateHandle.get<String>("dosage") ?: "")
                        )
                        TextButton(
                            onClick = {
                                entry.savedStateHandle["dosage"] = "2 tabletas"
                                entry.savedStateHandle["scheduleText"] = "08:15,20:15"
                                nav.navigate(reviewRoute)
                            }
                        ) {
                            Text("Revisar borrador")
                        }
                    }
                    composable(
                        RootDestination.TreatmentLifecycle.route,
                        arguments = listOf(navArgument("reminderDelay") { type = NavType.IntType }),
                    ) { entry ->
                        TreatmentLifecycleRoute(
                            TreatmentLifecycleViewModel.Factory(
                                "caregiver",
                                "adult",
                                "Rosa Vargas",
                                "medication",
                                "Losartán 50 mg",
                                "treatment",
                                "Control de presión",
                                "2 tabletas",
                                "Diaria",
                                "08:15,20:15",
                                "Con agua",
                                10,
                                ConfigureTreatmentCommandHandler(repository),
                                ChangeTreatmentStatusCommandHandler(repository),
                            ),
                            onCompleteConfiguration = { nav.completeTreatmentConfiguration(entry) },
                        )
                    }
                }
            }
        }
        if (existingWizard) compose.onNodeWithText("Revisar borrador").performClick()
        compose.onNodeWithText("Activar tratamiento").performScrollTo().performClick()
        compose.onNodeWithText("Configuración incompleta").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Activar tratamiento").assertDoesNotExist()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory =
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        File(directory, "treatment43-incomplete.png").outputStream().use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        compose.onNodeWithText("Completar configuración").performScrollTo().performClick()
        compose.onNodeWithText("Pauta conservada: 2 tabletas").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(listOf("configureTreatment"), calls)
            assertEquals("08:15,20:15", recoveredSchedule)
        }
    }
}
