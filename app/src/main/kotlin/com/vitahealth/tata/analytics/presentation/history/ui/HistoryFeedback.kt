package com.vitahealth.tata.analytics.presentation.history.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText

/** Loading and error states of the history flow. */

@Composable
internal fun LoadingCard() {
    TataCard(containerColor = TataLavender, modifier = Modifier.fillMaxWidth()) {
        Text("Historial", color = TataNavy, fontWeight = FontWeight.SemiBold)
        Text(
            text = "Consultando tu historial...",
            color = TataMuted,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
internal fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TataCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "No pudimos cargar el historial",
                color = TataText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = message, color = TataMuted, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(Modifier.height(14.dp))
        TataButton(text = "Reintentar", onClick = onRetry, style = TataButtonStyle.Secondary)
    }
}
