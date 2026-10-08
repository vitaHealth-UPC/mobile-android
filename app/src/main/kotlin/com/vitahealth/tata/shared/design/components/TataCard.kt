package com.vitahealth.tata.shared.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.shared.design.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.design.theme.TataHighContrastBorder

@Composable
fun TataCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val highContrast = LocalTataAccessibility.current.highContrast
    Column(
        modifier = modifier
            .background(containerColor, shape)
            .then(if (highContrast) Modifier.border(BorderStroke(1.5.dp, TataHighContrastBorder), shape) else Modifier)
            .padding(16.dp),
        content = content,
    )
}
