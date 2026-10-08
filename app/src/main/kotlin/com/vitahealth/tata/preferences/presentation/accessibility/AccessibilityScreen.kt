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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.tataPrototypeTopPadding
import com.vitahealth.tata.shared.design.theme.tataPrototypeShadow
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
import com.vitahealth.tata.R
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
import com.vitahealth.tata.shared.design.theme.TataSuccess
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
        onReducedMotionChange = viewModel::onReducedMotionChange,
        onVoiceConfirmationChange = viewModel::onVoiceConfirmationChange,
        onReadingAssistanceChange = viewModel::onReadingAssistanceChange,
        modifier = modifier,
    )
}

@Composable
fun AccessibilityScreen(
    state: AccessibilityUiState,
    onBack: () -> Unit,
    onLargeTextChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    onVoiceConfirmationChange: (Boolean) -> Unit,
    onReadingAssistanceChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHelp by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState())
        .padding(horizontal = 22.dp).padding(top = tataPrototypeTopPadding(), bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val backLabel = stringResource(R.string.accessibility_back)
            Box(Modifier.size(width = 38.dp, height = 36.dp).clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = backLabel }, contentAlignment = Alignment.CenterStart) {
                Text("‹", fontSize = 24.sp, color = TataNavy)
            }
            Text(stringResource(R.string.accessibility_title), fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif)),
                fontSize = 25.sp, lineHeight = 33.sp, color = tataTextColor())
        }
        AccessText(stringResource(R.string.accessibility_subtitle), 12, modifier = Modifier.padding(start = 38.dp, top = 2.dp))
        Spacer(Modifier.height(25.dp))
        SectionTitle(stringResource(R.string.accessibility_section_view))
        PreferenceGroup {
            PreferenceRow(R.string.accessibility_large_text_title, R.string.accessibility_large_text_subtitle,
                state.preferences.largeTextEnabled, onLargeTextChange, !state.isSaving,
                R.raw.figma_access_textsize, TataLavender)
            RowDivider()
            PreferenceRow(R.string.accessibility_contrast_title, R.string.accessibility_contrast_subtitle,
                state.preferences.highContrast, onHighContrastChange, !state.isSaving,
                R.raw.figma_access_contrast, TataBlueSurface)
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.accessibility_section_interaction))
        PreferenceGroup {
            PreferenceRow(R.string.accessibility_motion_title, R.string.accessibility_motion_subtitle,
                state.preferences.reducedMotion, onReducedMotionChange, !state.isSaving,
                R.raw.figma_access_reducemotion, TataMint)
            RowDivider()
            PreferenceRow(R.string.accessibility_voice_title, R.string.accessibility_voice_subtitle,
                state.preferences.voiceConfirmation, onVoiceConfirmationChange, !state.isSaving,
                R.raw.figma_access_voice, TataLavender)
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle(stringResource(R.string.accessibility_section_help))
        PreferenceGroup {
            PreferenceRow(R.string.accessibility_reading_title, R.string.accessibility_reading_subtitle,
                state.preferences.readingAssistance, onReadingAssistanceChange, !state.isSaving,
                R.raw.figma_access_read, TataMint, minHeight = 62)
        }
        Spacer(Modifier.height(25.dp))
        androidx.compose.material3.Surface(onClick = { showHelp = true }, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = Color.Transparent, modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))) {
            Row(Modifier.background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFEEF1FF), Color(0xFFE7E9FC))))
                .padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    AccessText(stringResource(R.string.accessibility_help_title), 13, TataDeepNavy, FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    AccessText(stringResource(R.string.accessibility_help_subtitle), 11)
                }
                AccessText("›", 18, TataDeepNavy)
            }
        }
        state.message?.let { message ->
            Spacer(Modifier.height(16.dp))
            MessageBanner(message, state.messageIsError)
        }
    }
    if (showHelp) androidx.compose.material3.AlertDialog(onDismissRequest = { showHelp = false },
        title = { Text(stringResource(R.string.accessibility_help_title)) },
        text = { Text(stringResource(R.string.accessibility_help_body)) },
        confirmButton = { androidx.compose.material3.TextButton(onClick = { showHelp = false }) {
            Text(stringResource(R.string.accessibility_help_close))
        } })
}

@Composable
private fun PreferenceGroup(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val highContrast = com.vitahealth.tata.shared.design.accessibility.LocalTataAccessibility.current.highContrast
    androidx.compose.material3.Surface(border = if (highContrast) androidx.compose.foundation.BorderStroke(1.dp, TataDeepNavy) else null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        color = Color.White, modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, androidx.compose.foundation.shape.RoundedCornerShape(18.dp))) {
        Column(content = content)
    }
}

@Composable
private fun PreferenceRow(title: Int, subtitle: Int, checked: Boolean, onChange: (Boolean) -> Unit,
    enabled: Boolean, icon: Int, badgeColor: Color, minHeight: Int = 58) {
    Row(Modifier.fillMaxWidth().heightIn(min = minHeight.dp)
        .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
        .padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(badgeColor, CircleShape), contentAlignment = Alignment.Center) {
            com.vitahealth.tata.shared.design.components.TataSvgIcon(
                resource = icon,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            AccessText(stringResource(title), 14, tataTextColor(), FontWeight.SemiBold)
            AccessText(stringResource(subtitle), 11, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(width = 48.dp, height = 28.dp)
            .background(if (checked) Color(0xFF203F76) else Color(0xFFE8EAF1), CircleShape),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart) {
            Box(Modifier.padding(horizontal = 2.dp).size(24.dp).background(Color.White, CircleShape))
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = Color(0xFFE3E5F0), modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun SectionTitle(text: String) {
    AccessText(text, 13, tataTextColor(), FontWeight.SemiBold, Modifier.padding(start = 2.dp, bottom = 8.dp))
}

@Composable
private fun AccessText(text: String, size: Int, color: Color = tataMutedColor(),
    weight: FontWeight = FontWeight.Normal, modifier: Modifier = Modifier) {
    val reading = com.vitahealth.tata.shared.design.accessibility.LocalTataAccessibility.current.readingAssistance
    Text(text, modifier, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_inter)),
        fontSize = size.sp, lineHeight = (size * if (reading) 1.6f else 1.3f).sp,
        letterSpacing = if (reading) 0.3.sp else 0.sp, fontWeight = if (reading && weight == FontWeight.Normal) FontWeight.Medium else weight), color = color)
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
            AccessibilityMessage.ReducedMotionSaved -> R.string.accessibility_motion_saved
            AccessibilityMessage.StandardMotionSaved -> R.string.accessibility_standard_motion_saved
            AccessibilityMessage.VoiceConfirmationSaved -> R.string.accessibility_voice_saved
            AccessibilityMessage.VoiceConfirmationOffSaved -> R.string.accessibility_voice_off_saved
            AccessibilityMessage.ReadingAssistanceSaved -> R.string.accessibility_reading_saved
            AccessibilityMessage.StandardReadingSaved -> R.string.accessibility_standard_reading_saved
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
            onReducedMotionChange = {},
            onVoiceConfirmationChange = {},
            onReadingAssistanceChange = {},
        )
    }
}
