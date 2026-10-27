package com.vitahealth.tata.identity.presentation.access

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Local-only UI test: no provider account, network or customer identity is used. */
class SocialIdentityLinkDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun caregiverCanLinkGoogleWithoutReplacingTheirTataSession() {
        validate(SocialProvider.GOOGLE)
    }

    @Test fun caregiverCanLinkFacebookWithoutReplacingTheirTataSession() {
        validate(SocialProvider.FACEBOOK)
    }

    @Test fun unavailableProviderDoesNotRequestCredentialsOrLinkAccount() {
        var launches = 0
        var links = 0
        val unavailable = object : SocialCredentialLauncher {
            override fun isAvailable(provider: SocialProvider) = false
            override suspend fun request(provider: SocialProvider, activity: ComponentActivity): String {
                launches++
                return "must-not-be-used"
            }
        }
        compose.setContent {
            TataTheme {
                SocialIdentityLinkDialog(
                    credentials = unavailable,
                    link = { links++; AppResult.Success(Unit) },
                    onDismiss = {},
                )
            }
        }
        compose.onNodeWithText("Google").performClick()
        compose.onNodeWithText(
            compose.activity.getString(R.string.access_error_social_unavailable)
        ).assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(0, launches)
            assertEquals(0, links)
        }
    }

    @Test fun rejectedProviderBindingDoesNotClaimSuccess() {
        var attempts = 0
        val credentials = object : SocialCredentialLauncher {
            override fun isAvailable(provider: SocialProvider) = true
            override suspend fun request(provider: SocialProvider, activity: ComponentActivity) =
                "verified-fixture-token"
        }
        compose.setContent {
            TataTheme {
                SocialIdentityLinkDialog(
                    credentials = credentials,
                    link = {
                        attempts++
                        AppResult.Failure("Already linked to a different account", code = "SOCIAL_LINK_CONFLICT")
                    },
                    onDismiss = {},
                )
            }
        }
        compose.onNodeWithText("Facebook").performClick()
        compose.onNodeWithText(
            compose.activity.getString(R.string.access_error_social_link)
        ).assertIsDisplayed()
        compose.onNodeWithText(
            compose.activity.getString(R.string.access_social_link_success)
        ).assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, attempts) }
    }

    private fun validate(provider: SocialProvider) {
        var chosen: SocialProvider? = null
        var linked = 0
        var closed = 0
        val providerGateway = object : SocialCredentialLauncher {
            override fun isAvailable(provider: SocialProvider) = true
            override suspend fun request(provider: SocialProvider, activity: ComponentActivity): String {
                chosen = provider
                return "signed-mock-provider-token"
            }
        }
        compose.setContent {
            TataTheme {
                SocialIdentityLinkDialog(
                    credentials = providerGateway,
                    link = { token ->
                        assertEquals("signed-mock-provider-token", token)
                        linked++
                        AppResult.Success(Unit)
                    },
                    onDismiss = { closed++ },
                )
            }
        }
        compose.onNodeWithText(provider.name.lowercase().replaceFirstChar(Char::uppercase)).performClick()
        compose.onNodeWithText(
            compose.activity.getString(R.string.access_social_link_success)
        ).assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.access_social_link_close)).performClick()
        compose.runOnIdle {
            assertEquals(provider, chosen)
            assertEquals(1, linked)
            assertEquals(1, closed)
        }
    }
}
