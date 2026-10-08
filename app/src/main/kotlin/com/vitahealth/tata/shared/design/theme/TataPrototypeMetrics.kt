package com.vitahealth.tata.shared.design.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Places the prototype heading at 56 dp while retaining native safe drawing insets. */
@Composable
fun tataPrototypeTopPadding(): Dp {
    val density = LocalDensity.current
    val inset = WindowInsets.safeDrawing.getTop(density)
    return (56.dp - with(density) { inset.toDp() }).coerceAtLeast(0.dp)
}

/** Figma's low-opacity navy shadow, instead of the opaque default elevation tint. */
fun Modifier.tataPrototypeShadow(elevation: Dp, shape: Shape): Modifier =
    shadow(elevation, shape, ambientColor = Color(0x331A2138), spotColor = Color(0x331A2138))
