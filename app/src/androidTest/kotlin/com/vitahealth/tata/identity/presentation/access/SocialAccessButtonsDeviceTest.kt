package com.vitahealth.tata.identity.presentation.access

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.vitahealth.tata.identity.presentation.access.screens.SessionAccessScreen
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessUiState
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Verifies both Figma social buttons call the actual provider route, not the placeholder modal. */
class SocialAccessButtonsDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun providerButtonsAreWiredToSeparateActions() {
        var google = 0
        var facebook = 0
        compose.setContent {
            TataTheme {
                SessionAccessScreen(
                    state = SessionAccessUiState(),
                    onEmail = {},
                    onPassword = {},
                    onSignIn = {},
                    onUseDemoAccount = {},
                    onRegister = {},
                    onPin = {},
                    showPin = false,
                    onGoogle = { google++ },
                    onFacebook = { facebook++ },
                )
            }
        }
        compose.onNodeWithText("Continuar con Google").performScrollTo().performClick()
        compose.onNodeWithText("Continuar con Facebook").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, google)
            assertEquals(1, facebook)
        }
    }
}
