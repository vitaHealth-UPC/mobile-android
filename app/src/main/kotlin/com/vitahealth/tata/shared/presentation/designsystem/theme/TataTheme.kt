package com.vitahealth.tata.shared.presentation.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.TataAccessibility

private val TataHighContrastColors =
    lightColorScheme(
        primary = TataDeepNavy,
        secondary = TataDeepNavy,
        background = Color.White,
        surface = Color.White,
        onPrimary = Color.White,
        onBackground = TataHighContrastText,
        onSurface = TataHighContrastText,
        outline = TataHighContrastBorder,
    )

private val TataLightColors =
    lightColorScheme(
        primary = TataNavy,
        secondary = TataPurple,
        background = TataSurface,
        surface = TataSurface,
        onPrimary = Color.White,
        onBackground = TataText,
        onSurface = TataText,
    )

private val TataDarkColors = darkColorScheme(
    primary = Color(0xFFACC8FF),
    secondary = Color(0xFFD4C5FF),
    background = Color(0xFF101725),
    surface = Color(0xFF182235),
    onPrimary = Color(0xFF102C54),
    onBackground = Color(0xFFF1F4FA),
    onSurface = Color(0xFFF1F4FA),
    onSurfaceVariant = Color(0xFFB9C5DA),
    outline = Color(0xFF8799B8),
)

@Composable
fun TataTheme(
    accessibility: TataAccessibility = TataAccessibility(),
    content: @Composable () -> Unit,
) {
    val systemDensity = LocalDensity.current
    // Only the font scale changes; layout dimensions in dp stay untouched.
    val scaledDensity =
        Density(
            density = systemDensity.density,
            fontScale = systemDensity.fontScale * accessibility.fontScale,
        )

    CompositionLocalProvider(
        LocalTataAccessibility provides accessibility,
        LocalDensity provides scaledDensity,
    ) {
        MaterialTheme(
            colorScheme =
                when {
                    accessibility.darkTheme -> TataDarkColors
                    accessibility.highContrast -> TataHighContrastColors
                    else -> TataLightColors
                },
            typography = tataTypography(accessibility.readingAssistance),
            content = content,
        )
    }
}

/** Secondary text colour. Prefer it over [TataMuted] on screens that must respect high contrast. */
@Composable
fun tataMutedColor(): Color =
    when {
        LocalTataAccessibility.current.darkTheme -> MaterialTheme.colorScheme.onSurfaceVariant
        LocalTataAccessibility.current.highContrast -> TataHighContrastMuted
        else -> TataMuted
    }

/** Body text colour that follows the high contrast setting. */
@Composable
fun tataTextColor(): Color =
    when {
        LocalTataAccessibility.current.darkTheme -> MaterialTheme.colorScheme.onSurface
        LocalTataAccessibility.current.highContrast -> TataHighContrastText
        else -> TataText
    }

/** Background token for screens that support the explicit application theme. */
@Composable
fun tataBackgroundColor(): Color = MaterialTheme.colorScheme.background

/** Card token, separate from the screen background in dark mode. */
@Composable
fun tataCardColor(): Color =
    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.surface else Color.White

/** Preserve the pastel Figma light surface while avoiding white cards in dark mode. */
@Composable
fun tataTonalColor(light: Color): Color =
    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.surfaceVariant else light

/** Original light border or theme outline in dark mode. */
@Composable
fun tataOutlineColor(light: Color = TataBorder): Color =
    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.outline else light

/** Highlight text on a card without imposing navy on a dark background. */
@Composable
fun tataAccentColor(light: Color = TataNavy): Color =
    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.primary else light
