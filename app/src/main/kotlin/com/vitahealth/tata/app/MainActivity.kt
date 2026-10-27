package com.vitahealth.tata.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.vitahealth.tata.app.navigation.TataNavHost
import com.vitahealth.tata.omission.application.readmodels.PushDestination
import com.vitahealth.tata.omission.presentation.notifications.mappers.OmissionPushIntent
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.preferences.infrastructure.local.AccountLanguageRepository
import com.vitahealth.tata.shared.infrastructure.security.EncryptedSessionStore
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility
import java.time.Instant
import java.util.Locale
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = runCatching {
            val current = EncryptedSessionStore(newBase).session()
            if (current != null && current.owner != null && current.expiresAt.isAfter(Instant.now())) {
                AccountLanguageRepository.languageFor(newBase, current.owner.storageKey)
            } else AppLanguage.SPANISH
        }.getOrDefault(AppLanguage.SPANISH)
        super.attachBaseContext(newBase.withTataAppLocale(language.tag))
    }

    /** Screen requested by a tapped omission notification, consumed once it is open. */
    private var pushDestination by mutableStateOf<PushDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val container = (application as TataApplication).container
        if (savedInstanceState == null) pushDestination = destinationOf(intent)

        setContent {
            val preferences by
                container.accessibilityPreferences.collectAsState(
                    initial = AccessibilityPreferences.Defaults
                )

            val language by container.languagePreferences.collectAsState(
                initial = container.initialLanguage(),
            )
            LaunchedEffect(language) {
                val requested = Locale.forLanguageTag(language.tag).language
                if (resources.configuration.locales[0].language != requested) recreate()
            }

            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !preferences.darkTheme
                    isAppearanceLightNavigationBars = !preferences.darkTheme
                }
            }

            TataTheme(
                accessibility =
                    TataAccessibility(
                        fontScale = preferences.textSize.scaleFactor,
                        highContrast = preferences.highContrast,
                        darkTheme = preferences.darkTheme,
                        reducedMotion = preferences.reducedMotion,
                        readingAssistance = preferences.readingAssistance,
                    )
            ) {
                TataNavHost(
                    container = container,
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    pushDestination = pushDestination,
                    onPushDestinationOpened = { pushDestination = null },
                )
            }
        }
    }

    @Deprecated("Facebook SDK activity result compatibility bridge")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (application as TataApplication).container.socialCredentials
            .onFacebookActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        destinationOf(intent)?.let { pushDestination = it }
    }

    private fun destinationOf(intent: Intent?): PushDestination? =
        (application as TataApplication)
            .container
            .pushDestination(
                intent?.getStringExtra(OmissionPushIntent.EXTRA_KIND),
                intent?.getStringExtra(OmissionPushIntent.EXTRA_OLDER_ADULT_ID),
                intent?.getStringExtra(OmissionPushIntent.EXTRA_MEDICATION),
                intent?.getStringExtra(OmissionPushIntent.EXTRA_INTAKE_ID),
            )
}
