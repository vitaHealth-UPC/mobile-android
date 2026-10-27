package com.vitahealth.tata.monitoring.presentation.notes

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.app.withSpanishAppLocale
import com.vitahealth.tata.monitoring.application.readmodels.MonitoredPerson
import com.vitahealth.tata.monitoring.domain.model.entities.FollowUpNote
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertsProblem
import com.vitahealth.tata.monitoring.presentation.notes.screens.NotesScreen
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Figma #27 semantics with authorized profile data; never used in production. */
@RunWith(AndroidJUnit4::class)
class NotesAuthorizedProfileFigmaTest {
    @get:Rule val compose = createComposeRule()

    private var addRequests = 0

    private fun show(state: NotesUiState) {
        compose.setContent {
            val original = LocalContext.current
            val spanish = remember(original) { original.withSpanishAppLocale() }
            CompositionLocalProvider(
                LocalContext provides spanish,
                LocalConfiguration provides spanish.resources.configuration,
            ) {
                TataTheme {
                    NotesScreen(
                        state = state,
                        caregiverId = "caregiver",
                        onAddNote = { addRequests++ },
                        person = MonitoredPerson("adult", "Rosa Vargas", LocalDate.of(1958, 5, 12)),
                    )
                }
            }
        }
    }

    @Test
    fun linkedNotesShowProfileAndNewNoteAction() {
        show(
            NotesUiState.Content(
                listOf(
                    FollowUpNote(
                        27,
                        "Prefiere una llamada cuando no responde.",
                        Instant.parse("2026-10-24T15:00:00Z"),
                        "caregiver",
                    )
                )
            )
        )
        compose.onNodeWithText("Rosa Vargas").assertIsDisplayed()
        compose.onNodeWithText("Activa").assertIsDisplayed()
        compose.onNodeWithText("Agregar nota").assertIsDisplayed()
        compose.onNodeWithText("Historial compartido").assertIsDisplayed()
        compose.onNodeWithText("Seguimiento y observaciones sobre Rosa Vargas.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Agregar nota").performClick()
        compose.runOnIdle { assertEquals(1, addRequests) }
        capture()
    }

    @Test
    fun deniedAccessDoesNotExposeCachedPersonOrActions() {
        show(NotesUiState.Error(AlertsProblem.ACCESS_DENIED))
        compose.onNodeWithText("Rosa Vargas").assertDoesNotExist()
        compose.onNodeWithText("Agregar nota").assertDoesNotExist()
    }

    private fun capture() {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val uri =
            requireNotNull(
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "monitoring-notes-profile.png")
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
