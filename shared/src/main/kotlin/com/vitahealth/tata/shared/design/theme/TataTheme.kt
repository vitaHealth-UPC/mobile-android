package com.vitahealth.tata.shared.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TataLightColors = lightColorScheme(
    primary = TataNavy,
    secondary = TataPurple,
    background = TataSurface,
    surface = TataSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = TataText,
    onSurface = TataText,
)

@Composable
fun TataTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TataLightColors,
        content = content,
    )
}
