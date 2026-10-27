package com.vitahealth.tata.treatment

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherencePoint
import com.vitahealth.tata.treatment.application.readmodels.TreatmentAdherenceReadModel
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentLifecycleScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentLifecycleUiState
import java.io.File
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

/** Complements the ViewModel refresh regression with actual native action availability. */
class TreatmentInactiveScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun inactiveMedicationPreservesHistoryAndCannotEditResumeOrDeactivateAgain() {
        var reactivationRequests = 0
        val forbidden = { error("Inactive medication cannot mutate") }
        compose.setContent {
            TataTheme {
                TreatmentLifecycleScreen(
                    TreatmentLifecycleUiState(
                        "caregiver",
                        "adult",
                        "Rosa Vargas",
                        "medication",
                        "Losartán 50 mg",
                        "treatment",
                        "Control de presión",
                        "1 tableta",
                        "Diaria",
                        "08:00",
                        "Con agua",
                        10,
                        status = TreatmentStatus.PAUSED,
                        activeMedicationCount = 0,
                        medicationInactive = true,
                        adherence =
                            TreatmentAdherenceReadModel(
                                92,
                                listOf(82, 92, 74, 96, 88, 94, 92).mapIndexed { index, percent ->
                                    TreatmentAdherencePoint(
                                        LocalDate.now().minusDays(6L - index),
                                        percent,
                                    )
                                },
                            ),
                    ),
                    onReactivateMedication = { reactivationRequests++ },
                    onActivate = forbidden,
                    onPause = forbidden,
                    onResume = forbidden,
                    onOpenDetail = {},
                    onEditRegimen = forbidden,
                    onManageMedication = forbidden,
                    navigationBar = { CaregiverTabBar(CaregiverTab.Person, {}) },
                )
            }
        }
        compose.onNodeWithText("Inactivo").assertExists()
        compose.onNodeWithText("0 medicamentos activos").assertExists()
        compose.onNodeWithText("Editar pauta").assertIsNotEnabled()
        compose.onNodeWithText("Reactivar").assertIsEnabled()
        compose
            .onAllNodesWithText("Medicamento desactivado")
            .onFirst()
            .performScrollTo()
            .assertIsNotEnabled()
        compose.onNodeWithText("Historial protegido").assertExists()
        compose.onNodeWithText("Persona").assertIsSelected().assertIsDisplayed()
        compose.onNodeWithText("92%").assertExists()
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory =
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        File(directory, "treatment38-inactive.png").outputStream().use {
            check(image.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        compose.onNodeWithText("Reactivar").performScrollTo().performClick()
        compose.runOnIdle { org.junit.Assert.assertEquals(1, reactivationRequests) }
    }
}
