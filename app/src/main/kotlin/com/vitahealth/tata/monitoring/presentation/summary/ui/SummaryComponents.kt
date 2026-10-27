package com.vitahealth.tata.monitoring.presentation.summary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import java.util.Locale

internal val summaryLocale = Locale.forLanguageTag("es-PE")

@Composable
internal fun SummaryCard(
    colors: List<Color>,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val surfaceColors =
        if (LocalTataAccessibility.current.darkTheme) {
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            colors
        }
    val click =
        if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    Column(
        Modifier.fillMaxWidth()
            .shadow(
                6.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(surfaceColors))
            .then(click)
            .padding(16.dp),
        content = content,
    )
}

@Composable
internal fun QuickAction(
    label: String,
    icon: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier
            .shadow(
                6.dp,
                RoundedCornerShape(16.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (LocalTataAccessibility.current.darkTheme) tataCardColor() else color)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 82.dp)
            .padding(vertical = 17.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TataSvgIcon(icon, Modifier.size(22.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = tataTextColor(),
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}
