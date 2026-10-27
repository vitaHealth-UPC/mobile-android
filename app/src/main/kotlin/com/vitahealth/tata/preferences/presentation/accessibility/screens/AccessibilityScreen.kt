package com.vitahealth.tata.preferences.presentation.accessibility.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.domain.model.valueobjects.AccessibilityPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.presentation.accessibility.ui.AccessText
import com.vitahealth.tata.preferences.presentation.accessibility.ui.MessageBanner
import com.vitahealth.tata.preferences.presentation.accessibility.ui.PreferenceGroup
import com.vitahealth.tata.preferences.presentation.accessibility.ui.PreferenceRow
import com.vitahealth.tata.preferences.presentation.accessibility.ui.RowDivider
import com.vitahealth.tata.preferences.presentation.accessibility.ui.SectionTitle
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityMessage
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityUiState
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBlueSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeTopPadding
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

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
    navigationBar: @Composable () -> Unit = {},
    onDarkThemeChange: (Boolean) -> Unit = {},
    onLanguageChange: () -> Unit = {},
) {
    var showHelp by androidx.compose.runtime.remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(tataBackgroundColor())) {
        Column(
            Modifier.weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = tataPrototypeTopPadding(), bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val backLabel = stringResource(R.string.accessibility_back)
                Box(
                    Modifier.size(48.dp).clickable(role = Role.Button, onClick = onBack).semantics {
                        contentDescription = backLabel
                    },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text("‹", fontSize = 24.sp, color = tataTextColor())
                }
                Text(
                    stringResource(R.string.accessibility_title),
                    fontFamily = FontFamily(Font(R.font.tata_serif)),
                    fontSize = 25.sp,
                    lineHeight = 33.sp,
                    color = tataTextColor(),
                )
            }
            AccessText(
                stringResource(R.string.accessibility_subtitle),
                12,
                modifier = Modifier.padding(start = 48.dp, top = 2.dp),
            )
            Spacer(Modifier.height(25.dp))
            SectionTitle(stringResource(R.string.accessibility_section_view))
            PreferenceGroup {
                PreferenceRow(
                    R.string.accessibility_large_text_title,
                    R.string.accessibility_large_text_subtitle,
                    state.preferences.largeTextEnabled,
                    onLargeTextChange,
                    !state.isSaving,
                    R.raw.figma_access_textsize,
                    TataLavender,
                )
                RowDivider()
                PreferenceRow(
                    R.string.accessibility_contrast_title,
                    R.string.accessibility_contrast_subtitle,
                    state.preferences.highContrast,
                    onHighContrastChange,
                    !state.isSaving,
                    R.raw.figma_access_contrast,
                    TataBlueSurface,
                )
                RowDivider()
                PreferenceRow(
                    R.string.accessibility_dark_theme_title,
                    R.string.accessibility_dark_theme_subtitle,
                    state.preferences.darkTheme,
                    onDarkThemeChange,
                    !state.isSaving,
                    R.raw.figma_access_darkmode,
                    TataLavender,
                )
            }
            Spacer(Modifier.height(24.dp))
            SectionTitle(stringResource(R.string.accessibility_section_interaction))
            PreferenceGroup {
                PreferenceRow(
                    R.string.accessibility_motion_title,
                    R.string.accessibility_motion_subtitle,
                    state.preferences.reducedMotion,
                    onReducedMotionChange,
                    !state.isSaving,
                    R.raw.figma_access_reducemotion,
                    TataMint,
                )
                RowDivider()
                PreferenceRow(
                    R.string.accessibility_voice_title,
                    R.string.accessibility_voice_subtitle,
                    state.preferences.voiceConfirmation,
                    onVoiceConfirmationChange,
                    !state.isSaving,
                    R.raw.figma_access_voice,
                    TataLavender,
                )
            }
            Spacer(Modifier.height(24.dp))
            SectionTitle(stringResource(R.string.accessibility_section_help))
            PreferenceGroup {
                PreferenceRow(
                    R.string.accessibility_reading_title,
                    R.string.accessibility_reading_subtitle,
                    state.preferences.readingAssistance,
                    onReadingAssistanceChange,
                    !state.isSaving,
                    R.raw.figma_access_read,
                    TataMint,
                    minHeight = 62,
                )
            }
            Spacer(Modifier.height(25.dp))
            Surface(
                onClick = { showHelp = true },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier =
                    Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(16.dp)),
            ) {
                Row(
                    Modifier.background(
                            Brush.horizontalGradient(
                                if (LocalTataAccessibility.current.darkTheme)
                                    listOf(tataCardColor(), tataCardColor())
                                else listOf(Color(0xFFEEF1FF), Color(0xFFE7E9FC))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        AccessText(
                            stringResource(R.string.accessibility_help_title),
                            13,
                            tataTextColor(),
                            FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(4.dp))
                        AccessText(stringResource(R.string.accessibility_help_subtitle), 11)
                    }
                    AccessText("›", 18, tataTextColor())
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth()
                    .tataPrototypeShadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = tataCardColor(),
            ) {
                Row(
                    Modifier.height(54.dp).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(34.dp).background(
                            TataLavender, RoundedCornerShape(50),
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        TataSvgIcon(R.raw.figma_access_language, Modifier.size(28.dp))
                    }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        AccessText(
                            stringResource(R.string.accessibility_language_title),
                            13, tataTextColor(), FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        AccessText(
                            stringResource(R.string.accessibility_language_subtitle),
                            9,
                        )
                    }
                    Surface(
                        onClick = onLanguageChange,
                        enabled = !state.isSaving && !state.isLanguageSaving,
                        shape = RoundedCornerShape(15.dp),
                        color = TataLavender,
                    ) {
                        AccessText(
                            "‹ " + state.appLanguage.displayName + " ›",
                            11, Color(0xFF29458C), FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        )
                    }
                }
            }
            state.languageError?.let {
                Spacer(Modifier.height(8.dp))
                AccessText(it, 11, Color(0xFFBC4545))
            }
            state.message?.let { message ->
                Spacer(Modifier.height(16.dp))
                MessageBanner(message, state.messageIsError)
            }
        }
        navigationBar()
    }
    if (showHelp)
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.accessibility_help_title)) },
            text = { Text(stringResource(R.string.accessibility_help_body)) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.accessibility_help_close))
                }
            },
        )
}

@Preview(showBackground = true)
@Composable
private fun AccessibilityScreenPreview() {
    TataTheme {
        AccessibilityScreen(
            state =
                AccessibilityUiState(
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
