package com.vitahealth.tata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.vitahealth.tata.app.navigation.TataNavHost
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.shared.design.accessibility.TataAccessibility
import com.vitahealth.tata.shared.design.theme.TataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as TataApplication).container

        setContent {
            val preferences by container.accessibilityPreferences
                .collectAsState(initial = AccessibilityPreferences.Defaults)

            TataTheme(accessibility = TataAccessibility(fontScale = preferences.textSize.scaleFactor)) {
                TataNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
