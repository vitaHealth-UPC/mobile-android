package com.vitahealth.tata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.vitahealth.tata.app.navigation.TataNavHost
import com.vitahealth.tata.shared.design.theme.TataTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TataTheme {
                TataNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
