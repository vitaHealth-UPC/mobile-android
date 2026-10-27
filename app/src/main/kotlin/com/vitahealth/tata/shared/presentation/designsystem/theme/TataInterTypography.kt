package com.vitahealth.tata.shared.presentation.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

/** Applies real Inter weights within one audited flow, not globally. */
@Composable
fun TataInterTypography(content: @Composable () -> Unit) {
    val base = MaterialTheme.typography
    MaterialTheme(
        typography =
            Typography(
                displayLarge = base.displayLarge.copy(fontFamily = TataInterFontFamily),
                displayMedium = base.displayMedium.copy(fontFamily = TataInterFontFamily),
                displaySmall = base.displaySmall.copy(fontFamily = TataInterFontFamily),
                headlineLarge = base.headlineLarge.copy(fontFamily = TataInterFontFamily),
                headlineMedium = base.headlineMedium.copy(fontFamily = TataInterFontFamily),
                headlineSmall = base.headlineSmall.copy(fontFamily = TataInterFontFamily),
                titleLarge = base.titleLarge.copy(fontFamily = TataInterFontFamily),
                titleMedium = base.titleMedium.copy(fontFamily = TataInterFontFamily),
                titleSmall = base.titleSmall.copy(fontFamily = TataInterFontFamily),
                bodyLarge = base.bodyLarge.copy(fontFamily = TataInterFontFamily),
                bodyMedium = base.bodyMedium.copy(fontFamily = TataInterFontFamily),
                bodySmall = base.bodySmall.copy(fontFamily = TataInterFontFamily),
                labelLarge = base.labelLarge.copy(fontFamily = TataInterFontFamily),
                labelMedium = base.labelMedium.copy(fontFamily = TataInterFontFamily),
                labelSmall = base.labelSmall.copy(fontFamily = TataInterFontFamily),
            ),
        content = content,
    )
}
