package com.vitahealth.tata.preferences.presentation.accessibility.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityMessage
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

@Composable
internal fun PreferenceGroup(content: @Composable ColumnScope.() -> Unit) {
    val highContrast = LocalTataAccessibility.current.highContrast
    Surface(
        border =
            if (highContrast)
                BorderStroke(
                    1.dp,
                    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.outline
                    else TataDeepNavy,
                )
            else null,
        shape = RoundedCornerShape(18.dp),
        color = tataCardColor(),
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp)),
    ) {
        Column(content = content)
    }
}

@Composable
internal fun PreferenceRow(
    title: Int,
    subtitle: Int,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean,
    icon: Int,
    badgeColor: Color,
    minHeight: Int = 58,
) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).background(badgeColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TataSvgIcon(resource = icon, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            AccessText(stringResource(title), 14, tataTextColor(), FontWeight.SemiBold)
            AccessText(stringResource(subtitle), 11, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier.size(width = 48.dp, height = 28.dp)
                .background(if (checked) Color(0xFF203F76) else Color(0xFFE8EAF1), CircleShape),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier.padding(horizontal = 2.dp).size(24.dp).background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
internal fun RowDivider() {
    HorizontalDivider(color = Color(0xFFE3E5F0), modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
internal fun SectionTitle(text: String) {
    AccessText(
        text,
        13,
        tataTextColor(),
        FontWeight.SemiBold,
        Modifier.padding(start = 2.dp, bottom = 8.dp),
    )
}

@Composable
internal fun AccessText(
    text: String,
    size: Int,
    color: Color = tataMutedColor(),
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
) {
    val reading = LocalTataAccessibility.current.readingAssistance
    Text(
        text,
        modifier,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily(Font(R.font.tata_inter)),
                fontSize = size.sp,
                lineHeight = (size * if (reading) 1.6f else 1.3f).sp,
                letterSpacing = if (reading) 0.3.sp else 0.sp,
                fontWeight =
                    if (reading && weight == FontWeight.Normal) FontWeight.Medium else weight,
            ),
        color = color,
    )
}

@Composable
internal fun MessageBanner(message: AccessibilityMessage, isError: Boolean) {
    val title =
        stringResource(
            when {
                isError -> R.string.accessibility_error_title
                message == AccessibilityMessage.HighContrastSaved ->
                    R.string.accessibility_contrast_saved_title
                message == AccessibilityMessage.ReducedMotionSaved ->
                    R.string.accessibility_motion_saved_title
                message == AccessibilityMessage.ReadingAssistanceSaved ->
                    R.string.accessibility_reading_saved_title
                else -> R.string.accessibility_saved_title
            }
        )
    val body =
        stringResource(
            when (message) {
                AccessibilityMessage.LargeTextSaved -> R.string.accessibility_large_text_saved
                AccessibilityMessage.StandardTextSaved -> R.string.accessibility_standard_text_saved
                AccessibilityMessage.DarkThemeSaved -> R.string.accessibility_dark_theme_saved
                AccessibilityMessage.LightThemeSaved -> R.string.accessibility_light_theme_saved
                AccessibilityMessage.HighContrastSaved -> R.string.accessibility_contrast_saved
                AccessibilityMessage.StandardContrastSaved ->
                    R.string.accessibility_standard_contrast_saved
                AccessibilityMessage.ReducedMotionSaved -> R.string.accessibility_motion_saved
                AccessibilityMessage.StandardMotionSaved ->
                    R.string.accessibility_standard_motion_saved
                AccessibilityMessage.VoiceConfirmationSaved -> R.string.accessibility_voice_saved
                AccessibilityMessage.VoiceConfirmationOffSaved ->
                    R.string.accessibility_voice_off_saved
                AccessibilityMessage.ReadingAssistanceSaved -> R.string.accessibility_reading_saved
                AccessibilityMessage.StandardReadingSaved ->
                    R.string.accessibility_standard_reading_saved
                AccessibilityMessage.SavedOffline -> R.string.accessibility_saved_offline
                AccessibilityMessage.ErrorRejected -> R.string.accessibility_error_rejected
                AccessibilityMessage.ErrorUser -> R.string.accessibility_error_user
                AccessibilityMessage.ErrorGeneric -> R.string.accessibility_error_generic
            }
        )
    TataCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (isError) TataErrorSurface else TataMint,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily(Font(R.font.tata_inter)),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    ),
                fontWeight = FontWeight.SemiBold,
                color = if (isError) TataError else TataSuccess,
            )
            Text(
                text = body,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily(Font(R.font.tata_inter)),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    ),
                color = if (isError) TataError else Color(0xFF536273),
            )
        }
    }
}
