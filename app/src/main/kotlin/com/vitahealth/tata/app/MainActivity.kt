package com.vitahealth.tata.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.vitahealth.tata.app.navigation.TataNavHost
import com.vitahealth.tata.omission.application.queries.PushDestination
import com.vitahealth.tata.omission.presentation.notifications.OmissionPushIntent
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.shared.design.accessibility.TataAccessibility
import com.vitahealth.tata.shared.design.theme.TataTheme

class MainActivity : ComponentActivity() {
    /** Screen requested by a tapped omission notification, consumed once it is open. */
    private var pushDestination by mutableStateOf<PushDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as TataApplication).container
        if (savedInstanceState == null) pushDestination = destinationOf(intent)

        setContent {
            val preferences by container.accessibilityPreferences
                .collectAsState(initial = AccessibilityPreferences.Defaults)

            TataTheme(accessibility = TataAccessibility(
                    fontScale = preferences.textSize.scaleFactor,
                    highContrast = preferences.highContrast,
                    reducedMotion = preferences.reducedMotion,
                    readingAssistance = preferences.readingAssistance,
                )) {
                TataNavHost(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    pushDestination = pushDestination,
                    onPushDestinationOpened = { pushDestination = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        destinationOf(intent)?.let { pushDestination = it }
    }

    private fun destinationOf(intent: Intent?): PushDestination? = (application as TataApplication).container.pushDestination(
        intent?.getStringExtra(OmissionPushIntent.EXTRA_KIND),
        intent?.getStringExtra(OmissionPushIntent.EXTRA_OLDER_ADULT_ID),
        intent?.getStringExtra(OmissionPushIntent.EXTRA_MEDICATION),
    )
}
