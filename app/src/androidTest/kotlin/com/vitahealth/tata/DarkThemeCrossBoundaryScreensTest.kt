package com.vitahealth.tata

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.carelink.presentation.link.screens.CareLinkScreen
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkUiState
import com.vitahealth.tata.monitoring.presentation.notes.screens.NotesScreen
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.treatment.presentation.treatment.screens.TreatmentListScreen
import com.vitahealth.tata.treatment.presentation.treatment.viewmodel.TreatmentListState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Dark theme surfaces for shared, Care Link, Treatment and Monitoring routes.
 *
 * Uses the real production composables with test states. Does not certify a
 * backend session or constitute Figma acceptance by itself.
 */
@RunWith(AndroidJUnit4::class)
class DarkThemeCrossBoundaryScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun caregiverLinkUsesDarkBackgroundAndKeepsInputsReadable() {
        render(dark = true) {
            CareLinkScreen(
                state = CareLinkUiState(caregiverId = "caregiver-test"),
                onCodeChange = {},
                onSendRequest = {},
                onAcceptConsent = {},
                onRejectConsent = {},
            )
        }
        compose.onNodeWithText("Vincula a tu familiar").assertIsDisplayed()
        captureAndCheck("carelink-dark", dark = true)
    }

    @Test
    fun treatmentListUsesDarkBackgroundAndShowsEmptyState() {
        render(dark = true) {
            TreatmentListScreen(
                state = TreatmentListState(loading = false),
                olderAdultName = "Rosa",
                onOpen = {},
                onAddMedication = {},
                onBack = {},
                onRetry = {},
            )
        }
        compose.onNodeWithText("Tratamientos de Rosa").assertIsDisplayed()
        captureAndCheck("treatment-dark", dark = true)
    }

    @Test
    fun notesUseDarkBackgroundAndAccessibleNavigation() {
        render(dark = true) {
            NotesScreen(NotesUiState.Empty, caregiverId = "caregiver-test")
        }
        compose.onNodeWithText("Agregar nota").assertIsDisplayed()
        captureAndCheck("notes-dark", dark = true)
    }

    @Test
    fun lightModeRetainsFigmaSurfacePalette() {
        render(dark = false) {
            CareLinkScreen(
                state = CareLinkUiState(caregiverId = "caregiver-test"),
                onCodeChange = {},
                onSendRequest = {},
                onAcceptConsent = {},
                onRejectConsent = {},
            )
        }
        compose.onNodeWithText("Vincula a tu familiar").assertIsDisplayed()
        captureAndCheck("carelink-light-unchanged", dark = false)
    }

    private fun render(dark: Boolean, screen: @Composable () -> Unit) {
        compose.setContent {
            val original = LocalContext.current
            val spanish = remember(original) { original.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme(accessibility = TataAccessibility(darkTheme = dark)) {
                    screen()
                }
            }
        }
    }

    private fun captureAndCheck(name: String, dark: Boolean) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val edge = bitmap.getPixel(4, bitmap.height / 2)
        val lightness =
            (Color.red(edge) + Color.green(edge) + Color.blue(edge)) / 3
        assertTrue(
            "Unexpected surface luminance " + lightness,
            if (dark) lightness < 105 else lightness > 210,
        )

        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri = requireNotNull(
            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "tata-theme-" + name + ".png")
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
