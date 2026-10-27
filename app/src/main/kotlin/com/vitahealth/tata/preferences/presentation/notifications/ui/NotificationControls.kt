package com.vitahealth.tata.preferences.presentation.notifications.ui

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.presentation.notifications.mappers.formatTime
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.components.TataToggleRow
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBlueSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

@Composable
internal fun QuietHoursCard(
    preferences: NotificationPreferences,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onStartChange: (Int, Int) -> Unit,
    onEndChange: (Int, Int) -> Unit,
) {
    val hours = preferences.quietHours
    val context = LocalContext.current
    var editing by rememberSaveable { mutableStateOf(false) }
    TataCard(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(
                    6.dp,
                    RoundedCornerShape(18.dp),
                    ambientColor = Color(0x141A2138),
                    spotColor = Color(0x141A2138),
                )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f).clickable(
                    enabled = enabled && hours != null,
                    role = Role.Button,
                ) {
                    editing = true
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    TataSvgIcon(R.raw.notifications_quiet, Modifier.size(22.dp))
                    Text(
                        context.getString(R.string.notifications_quiet_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tataTextColor(),
                    )
                }
                Text(
                    hours?.let {
                        context.getString(
                            R.string.notifications_quiet_range,
                            formatTime(it.startHour, it.startMinute),
                            formatTime(it.endHour, it.endMinute),
                        )
                    } ?: context.getString(R.string.notifications_quiet_off),
                    fontSize = 11.sp,
                    color = tataMutedColor(),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Switch(
                checked = hours != null,
                onCheckedChange = onEnabledChange,
                enabled = enabled,
                modifier =
                    Modifier.size(48.dp, 28.dp).semantics {
                        contentDescription = context.getString(R.string.notifications_quiet_title)
                    },
                colors =
                    SwitchDefaults.colors(
                        checkedTrackColor = TataSuccess,
                        checkedThumbColor = Color.White,
                    ),
            )
        }
    }
    if (editing && hours != null) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text(context.getString(R.string.notifications_quiet_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TataButton(
                        context.getString(
                            R.string.notifications_quiet_from,
                            formatTime(hours.startHour, hours.startMinute),
                        ),
                        onClick = {
                            pickTime(context, hours.startHour, hours.startMinute, onStartChange)
                        },
                        enabled = enabled,
                        style = TataButtonStyle.Secondary,
                    )
                    TataButton(
                        context.getString(
                            R.string.notifications_quiet_to,
                            formatTime(hours.endHour, hours.endMinute),
                        ),
                        onClick = {
                            pickTime(context, hours.endHour, hours.endMinute, onEndChange)
                        },
                        enabled = enabled,
                        style = TataButtonStyle.Secondary,
                    )
                    Text(
                        context.getString(R.string.notifications_quiet_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { editing = false }) {
                    Text(context.getString(R.string.accessibility_help_close))
                }
            },
        )
    }
}

@Composable
internal fun ChannelsCard(
    preferences: NotificationPreferences,
    enabled: Boolean,
    onChannelChange: (ChannelType, Boolean) -> Unit,
) {
    val context = LocalContext.current
    var editing by rememberSaveable { mutableStateOf(false) }
    TataCard(
        modifier =
            Modifier.fillMaxWidth()
                .shadow(
                    6.dp,
                    RoundedCornerShape(18.dp),
                    ambientColor = Color(0x141A2138),
                    spotColor = Color(0x141A2138),
                )
                .clickable(enabled = enabled, role = Role.Button) { editing = true }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TataSvgIcon(R.raw.notifications_channels, Modifier.size(22.dp))
            Text(
                context.getString(R.string.notifications_channels_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = tataTextColor(),
                modifier = Modifier.weight(1f).padding(start = 7.dp),
            )
            Text(
                context.getString(R.string.notifications_edit),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ChannelType.entries.forEach { type ->
                val (title, _, icon) = channelTexts(type)
                Column(
                    Modifier.weight(1f)
                        .clickable(enabled = enabled, role = Role.Button) { editing = true }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    IconBadge(
                        icon,
                        when (type) {
                            ChannelType.PUSH -> TataBlueSurface
                            ChannelType.SMS -> TataMint
                            ChannelType.EMAIL -> TataLavender
                            ChannelType.CALL -> TataBlueSurface
                        },
                    )
                    Text(
                        context.getString(
                            if (type == ChannelType.SMS) R.string.notifications_sms_short else title
                        ),
                        fontSize = 9.sp,
                        lineHeight = 12.sp,
                        textAlign = TextAlign.Center,
                        color = tataTextColor(),
                        modifier = Modifier.requiredWidth(87.dp).padding(top = 4.dp),
                    )
                }
            }
        }
    }
    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text(context.getString(R.string.notifications_channels_title)) },
            text = {
                Column {
                    ChannelType.entries.forEach { type ->
                        val (title, subtitle, _) = channelTexts(type)
                        TataToggleRow(
                            context.getString(title),
                            context.getString(subtitle),
                            checked =
                                preferences.channels.firstOrNull { it.type == type }?.enabled
                                    ?: false,
                            onCheckedChange = { onChannelChange(type, it) },
                            enabled = enabled,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editing = false }) {
                    Text(context.getString(R.string.accessibility_help_close))
                }
            },
        )
    }
}

private data class ChannelTexts(val title: Int, val subtitle: Int, @param:RawRes val icon: Int)

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
        ChannelType.CALL ->
            ChannelTexts(
                R.string.notifications_channel_call,
                R.string.notifications_channel_call_hint,
                R.raw.notifications_call,
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
internal fun MessageBanner(message: NotificationMessage, isError: Boolean) {
    val title =
        stringResource(
            if (isError) R.string.accessibility_error_title else R.string.notifications_saved_title
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
    if (message == NotificationMessage.Saved) {
        Column(
            Modifier.fillMaxWidth()
                .background(TataMint, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                title,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TataSuccess,
            )
            Text(body, fontSize = 8.5.sp, lineHeight = 12.sp, color = tataMutedColor())
        }
        return
    }
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

private fun pickTime(context: Context, hour: Int, minute: Int, onPicked: (Int, Int) -> Unit) {
    TimePickerDialog(
            context,
            { _, pickedHour, pickedMinute -> onPicked(pickedHour, pickedMinute) },
            hour,
            minute,
            DateFormat.is24HourFormat(context),
        )
        .show()
}
