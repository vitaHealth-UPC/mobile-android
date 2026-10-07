package com.vitahealth.tata.preferences.presentation.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.preferences.R
import com.vitahealth.tata.preferences.domain.model.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataToggleRow
import com.vitahealth.tata.shared.design.theme.TataBlueSurface
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataPurple
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataMutedColor
import com.vitahealth.tata.shared.design.theme.tataTextColor

@Composable
fun AccessibilityRoute(
    factory: AccessibilityViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AccessibilityViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    AccessibilityScreen(
        state = state,
        onBack = onBack,
        onLargeTextChange = viewModel::onLargeTextChange,
        onHighContrastChange = viewModel::onHighContrastChange,
        modifier = modifier,
    )
}

@Composable
fun AccessibilityScreen(
    state: AccessibilityUiState,
    onBack: () -> Unit,
    onLargeTextChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val backLabel = stringResource(R.string.accessibility_back)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = backLabel },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "‹", fontSize = 28.sp, color = TataNavy)
            }
            Text(
                text = stringResource(R.string.accessibility_title),
                fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
                fontSize = 29.sp,
                color = tataTextColor(),
            )
        }
        Text(
            text = stringResource(R.string.accessibility_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = tataMutedColor(),
            modifier = Modifier.padding(start = 48.dp, bottom = 20.dp),
        )

        SectionTitle(stringResource(R.string.accessibility_section_view))
        TataCard(modifier = Modifier.fillMaxWidth()) {
            TataToggleRow(
                title = stringResource(R.string.accessibility_large_text_title),
                subtitle = stringResource(R.string.accessibility_large_text_subtitle),
                checked = state.preferences.largeTextEnabled,
                onCheckedChange = onLargeTextChange,
                enabled = !state.isSaving,
                leading = { IconBadge(glyph = "A", background = TataLavender, tint = TataPurple) },
            )
            RowDivider()
            TataToggleRow(
                title = stringResource(R.string.accessibility_contrast_title),
                subtitle = stringResource(R.string.accessibility_contrast_subtitle),
                checked = state.preferences.highContrast,
                onCheckedChange = onHighContrastChange,
                enabled = !state.isSaving,
                leading = { IconBadge(glyph = "◐", background = TataBlueSurface, tint = TataNavy) },
            )
        }

        state.message?.let { message ->
            Spacer(Modifier.height(16.dp))
            MessageBanner(message = message, isError = state.messageIsError)
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = TataBorder, modifier = Modifier.padding(start = 58.dp))
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = tataTextColor(),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun IconBadge(glyph: String, background: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = glyph, color = tint, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MessageBanner(message: AccessibilityMessage, isError: Boolean) {
    val title = stringResource(
        if (isError) R.string.accessibility_error_title else R.string.accessibility_saved_title,
    )
    val body = stringResource(
        when (message) {
            AccessibilityMessage.LargeTextSaved -> R.string.accessibility_large_text_saved
            AccessibilityMessage.StandardTextSaved -> R.string.accessibility_standard_text_saved
            AccessibilityMessage.HighContrastSaved -> R.string.accessibility_contrast_saved
            AccessibilityMessage.StandardContrastSaved -> R.string.accessibility_standard_contrast_saved
            AccessibilityMessage.SavedOffline -> R.string.accessibility_saved_offline
            AccessibilityMessage.ErrorRejected -> R.string.accessibility_error_rejected
            AccessibilityMessage.ErrorUser -> R.string.accessibility_error_user
            AccessibilityMessage.ErrorGeneric -> R.string.accessibility_error_generic
        },
    )
    TataCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (isError) TataErrorSurface else TataMint,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) TataError else tataTextColor(),
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) TataError else tataMutedColor(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessibilityScreenPreview() {
    TataTheme {
        AccessibilityScreen(
            state = AccessibilityUiState(
                preferences = AccessibilityPreferences(textSize = TextSizeLevel.LARGE),
                message = AccessibilityMessage.LargeTextSaved,
            ),
            onBack = {},
            onLargeTextChange = {},
            onHighContrastChange = {},
        )
    }
}
