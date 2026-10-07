package com.vitahealth.tata.shared.design.accessibility

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Accessibility settings that the design system applies to every screen.
 * It carries no business rule: `:preferences` decides the values and `:app` injects them.
 */
data class TataAccessibility(
    /** Multiplies the system font scale, so a user's OS-level setting is respected on top of it. */
    val fontScale: Float = 1f,
)

val LocalTataAccessibility = staticCompositionLocalOf { TataAccessibility() }
