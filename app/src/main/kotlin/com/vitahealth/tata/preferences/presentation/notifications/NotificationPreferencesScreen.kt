package com.vitahealth.tata.preferences.presentation.notifications

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.annotation.RawRes
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
import androidx.compose.ui.platform.LocalContext
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
import com.vitahealth.tata.preferences.domain.model.ChannelType
import com.vitahealth.tata.preferences.domain.model.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataButtonStyle
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataSvgIcon
import com.vitahealth.tata.shared.design.components.TataToggleRow
import com.vitahealth.tata.shared.design.theme.TataBlueSurface
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSuccess
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.tataMutedColor
import com.vitahealth.tata.shared.design.theme.tataTextColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun NotificationPreferencesRoute(
    factory: NotificationPreferencesViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: NotificationPreferencesViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    NotificationPreferencesScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onQuietHoursEnabledChange = viewModel::onQuietHoursEnabledChange,
        onQuietHoursStartChange = viewModel::onQuietHoursStartChange,
        onQuietHoursEndChange = viewModel::onQuietHoursEndChange,
        onChannelChange = viewModel::onChannelChange,
        modifier = modifier,
    )
}

@Composable
fun NotificationPreferencesScreen(
    state: NotificationPreferencesUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onQuietHoursEnabledChange: (Boolean) -> Unit,
    onQuietHoursStartChange: (Int, Int) -> Unit,
    onQuietHoursEndChange: (Int, Int) -> Unit,
    onChannelChange: (ChannelType, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TataSurface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val backLabel = stringResource(R.string.accessibility_back)
            Box(
                modifier =
                    Modifier.size(48.dp).clickable(role = Role.Button, onClick = onBack).semantics {
                        contentDescription = backLabel
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "‹", fontSize = 28.sp, color = TataNavy)
            }
            Text(
                text = stringResource(R.string.notifications_title),
                fontFamily = FontFamily(Font(com.vitahealth.tata.R.font.tata_serif)),
                fontSize = 21.sp,
                lineHeight = 26.sp,
                color = tataTextColor(),
            )
        }
        Spacer(Modifier.height(16.dp))

        val preferences = state.preferences
        when {
            state.isLoading ->
                TataCard(Modifier.fillMaxWidth(), TataMint) {
                    Text(stringResource(R.string.notifications_loading), color = tataTextColor())
                }

            preferences == null -> {
                state.message?.let { MessageBanner(it, state.messageIsError) }
                Spacer(Modifier.height(12.dp))
                TataButton(text = stringResource(R.string.notifications_retry), onClick = onRetry)
            }

            else -> {
                QuietHoursCard(
                    preferences = preferences,
                    enabled = !state.isSaving,
                    onEnabledChange = onQuietHoursEnabledChange,
                    onStartChange = onQuietHoursStartChange,
                    onEndChange = onQuietHoursEndChange,
                )
                Spacer(Modifier.height(20.dp))
                ChannelsCard(
                    preferences = preferences,
                    enabled = !state.isSaving,
                    onChannelChange = onChannelChange,
                )
                state.message?.let {
                    Spacer(Modifier.height(16.dp))
                    MessageBanner(it, state.messageIsError)
                }
            }
        }
    }
}

@Composable
private fun QuietHoursCard(
    preferences: NotificationPreferences,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onStartChange: (Int, Int) -> Unit,
    onEndChange: (Int, Int) -> Unit,
) {
    val hours = preferences.quietHours
    TataCard(modifier = Modifier.fillMaxWidth()) {
        TataToggleRow(
            title = stringResource(R.string.notifications_quiet_title),
            subtitle =
                hours?.let {
                    stringResource(
                        R.string.notifications_quiet_range,
                        formatTime(it.startHour, it.startMinute),
                        formatTime(it.endHour, it.endMinute),
                    )
                } ?: stringResource(R.string.notifications_quiet_off),
            checked = hours != null,
            onCheckedChange = onEnabledChange,
            enabled = enabled,
            leading = {
                TataSvgIcon(R.raw.notifications_quiet, Modifier.size(22.dp))
            },
        )
        if (hours != null) {
            val context = LocalContext.current
            Spacer(Modifier.height(8.dp))
            TataButton(
                text =
                    stringResource(
                        R.string.notifications_quiet_from,
                        formatTime(hours.startHour, hours.startMinute),
                    ),
                onClick = { pickTime(context, hours.startHour, hours.startMinute, onStartChange) },
                enabled = enabled,
                style = TataButtonStyle.Secondary,
            )
            Spacer(Modifier.height(8.dp))
            TataButton(
                text =
                    stringResource(
                        R.string.notifications_quiet_to,
                        formatTime(hours.endHour, hours.endMinute),
                    ),
                onClick = { pickTime(context, hours.endHour, hours.endMinute, onEndChange) },
                enabled = enabled,
                style = TataButtonStyle.Secondary,
            )
            Text(
                text = stringResource(R.string.notifications_quiet_hint),
                style = MaterialTheme.typography.bodySmall,
                color = tataMutedColor(),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ChannelsCard(
    preferences: NotificationPreferences,
    enabled: Boolean,
    onChannelChange: (ChannelType, Boolean) -> Unit,
) {
    Text(
        text = stringResource(R.string.notifications_channels_title),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = tataTextColor(),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
    TataCard(modifier = Modifier.fillMaxWidth()) {
        val channels = ChannelType.entries
        channels.forEachIndexed { index, type ->
            val (title, subtitle, icon) = channelTexts(type)
            TataToggleRow(
                title = stringResource(title),
                subtitle = stringResource(subtitle),
                checked = preferences.channels.firstOrNull { it.type == type }?.enabled ?: false,
                onCheckedChange = { onChannelChange(type, it) },
                enabled = enabled,
                leading = {
                    IconBadge(
                        icon = icon,
                        background =
                            when (type) {
                                ChannelType.PUSH -> TataBlueSurface
                                ChannelType.SMS -> TataMint
                                ChannelType.EMAIL -> TataLavender
                            },
                    )
                },
            )
            if (index != channels.lastIndex) {
                HorizontalDivider(color = TataBorder, modifier = Modifier.padding(start = 58.dp))
            }
        }
    }
}

private data class ChannelTexts(val title: Int, val subtitle: Int, @RawRes val icon: Int)

private fun channelTexts(type: ChannelType): ChannelTexts =
    when (type) {
        ChannelType.PUSH ->
            ChannelTexts(
                R.string.notifications_channel_push,
                R.string.notifications_channel_push_hint,
                R.raw.notifications_push,
            )
        ChannelType.SMS ->
            ChannelTexts(
                R.string.notifications_channel_sms,
                R.string.notifications_channel_sms_hint,
                R.raw.notifications_sms,
            )
        ChannelType.EMAIL ->
            ChannelTexts(
                R.string.notifications_channel_email,
                R.string.notifications_channel_email_hint,
                R.raw.notifications_email,
            )
    }

@Composable
private fun IconBadge(@RawRes icon: Int, background: Color) {
    Box(
        modifier = Modifier.size(36.dp).background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        TataSvgIcon(icon, Modifier.size(24.dp))
    }
}

@Composable
private fun MessageBanner(message: NotificationMessage, isError: Boolean) {
    val title =
        stringResource(
            if (isError) R.string.accessibility_error_title else R.string.accessibility_saved_title
        )
    val body =
        stringResource(
            when (message) {
                NotificationMessage.Saved -> R.string.notifications_saved
                NotificationMessage.ErrorInvalidHours -> R.string.notifications_error_hours
                NotificationMessage.ErrorRejected -> R.string.accessibility_error_rejected
                NotificationMessage.ErrorOffline -> R.string.notifications_error_offline
                NotificationMessage.ErrorUser -> R.string.accessibility_error_user
                NotificationMessage.ErrorGeneric -> R.string.accessibility_error_generic
            }
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
                color = if (isError) TataError else TataSuccess,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) TataError else tataMutedColor(),
            )
        }
    }
}

@Composable
private fun formatTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

private fun pickTime(
    context: android.content.Context,
    hour: Int,
    minute: Int,
    onPicked: (Int, Int) -> Unit,
) {
    TimePickerDialog(
            context,
            { _, pickedHour, pickedMinute -> onPicked(pickedHour, pickedMinute) },
            hour,
            minute,
            DateFormat.is24HourFormat(context),
        )
        .show()
}

@Preview(showBackground = true)
@Composable
private fun NotificationPreferencesScreenPreview() {
    TataTheme {
        NotificationPreferencesScreen(
            state =
                NotificationPreferencesUiState(
                    isLoading = false,
                    preferences =
                        NotificationPreferences(
                            quietHours = QuietHours.Default,
                            channels =
                                listOf(
                                    NotificationChannel(ChannelType.PUSH, true),
                                    NotificationChannel(ChannelType.SMS, false),
                                    NotificationChannel(ChannelType.EMAIL, false),
                                ),
                        ),
                    message = NotificationMessage.Saved,
                ),
            onBack = {},
            onRetry = {},
            onQuietHoursEnabledChange = {},
            onQuietHoursStartChange = { _, _ -> },
            onQuietHoursEndChange = { _, _ -> },
            onChannelChange = { _, _ -> },
        )
    }
}
