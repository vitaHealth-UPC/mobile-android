package com.vitahealth.tata.shared.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.vitahealth.tata.shared.design.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.design.accessibility.TataAccessibility

private val TataHighContrastColors = lightColorScheme(
    primary = TataDeepNavy,
    secondary = TataDeepNavy,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TataHighContrastText,
    onSurface = TataHighContrastText,
    outline = TataHighContrastBorder,
)

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
fun TataTheme(
    accessibility: TataAccessibility = TataAccessibility(),
    content: @Composable () -> Unit,
) {
    val systemDensity = LocalDensity.current
    // Only the font scale changes; layout dimensions in dp stay untouched.
    val scaledDensity = Density(
        density = systemDensity.density,
        fontScale = systemDensity.fontScale * accessibility.fontScale,
    )

    CompositionLocalProvider(
        LocalTataAccessibility provides accessibility,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(
            colorScheme = if (accessibility.highContrast) TataHighContrastColors else TataLightColors,
            content = content,
        )
    }
}

/** Secondary text colour. Prefer it over [TataMuted] on screens that must respect high contrast. */
@Composable
fun tataMutedColor(): Color =
    if (LocalTataAccessibility.current.highContrast) TataHighContrastMuted else TataMuted

/** Body text colour that follows the high contrast setting. */
@Composable
fun tataTextColor(): Color =
    if (LocalTataAccessibility.current.highContrast) TataHighContrastText else TataText
