package com.vitahealth.tata.shared.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val LINE_HEIGHT_FACTOR = 1.6f
private const val EXTRA_LETTER_SPACING = 0.3f

/** Lifts a style one weight step (never below Medium), widens the line height and the letter spacing. */
internal fun TextStyle.withReadingAssistance(): TextStyle {
    val weight = when {
        fontWeight == null || fontWeight!!.weight < FontWeight.Medium.weight -> FontWeight.Medium
        else -> fontWeight
    }
    return copy(
        fontWeight = weight,
        lineHeight = fontSize * LINE_HEIGHT_FACTOR,
        letterSpacing = (letterSpacing.value + EXTRA_LETTER_SPACING).sp,
    )
}

internal fun tataTypography(readingAssistance: Boolean): Typography {
    val base = Typography()
    if (!readingAssistance) return base
    return base.copy(
        titleLarge = base.titleLarge.withReadingAssistance(),
        titleMedium = base.titleMedium.withReadingAssistance(),
        titleSmall = base.titleSmall.withReadingAssistance(),
        bodyLarge = base.bodyLarge.withReadingAssistance(),
        bodyMedium = base.bodyMedium.withReadingAssistance(),
        bodySmall = base.bodySmall.withReadingAssistance(),
        labelLarge = base.labelLarge.withReadingAssistance(),
        labelMedium = base.labelMedium.withReadingAssistance(),
        labelSmall = base.labelSmall.withReadingAssistance(),
    )
}
