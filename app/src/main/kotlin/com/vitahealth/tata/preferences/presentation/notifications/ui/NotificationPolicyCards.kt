package com.vitahealth.tata.preferences.presentation.notifications.ui

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import java.io.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class PolicyGroup {
    Reminders,
    Caregiver,
    Emergency,
}

/** Three settings groups from Figma, backed by the user's stored notification policy. */
@Composable
internal fun NotificationPolicyCards(
    policy: NotificationPolicy,
    contacts: List<EmergencyContact>,
    enabled: Boolean,
    saveMessage: NotificationMessage?,
    onSave: (NotificationPolicy, List<EmergencyContact>) -> Unit,
) {
    val context = LocalContext.current
    var editing by rememberSaveable { mutableStateOf<PolicyGroup?>(null) }
    val locale = context.resources.configuration.locales[0]
    val summary =
        policy.dailySummaryHour?.let {
            LocalTime.of(it, requireNotNull(policy.dailySummaryMinute))
                .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        } ?: context.getString(R.string.notifications_policy_summary_off)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PolicyCard(
            context.getString(R.string.notifications_policy_reminders),
            R.raw.notifications_reminder,
            enabled,
            { editing = PolicyGroup.Reminders },
        ) {
            PolicyRow(
                context.getString(R.string.notifications_policy_cadence),
                context.getString(
                    R.string.notifications_policy_cadence_value,
                    policy.reminderMinutes,
                ),
            )
        }
        PolicyCard(
            context.getString(R.string.notifications_policy_caregiver),
            R.raw.notifications_caregiver,
            enabled,
            { editing = PolicyGroup.Caregiver },
        ) {
            PolicyRow(
                context.getString(R.string.notifications_policy_pending),
                delayLabel(policy.pendingAlertMinutes),
            )
            HorizontalDivider(color = tataMutedColor().copy(alpha = 0.18f))
            PolicyRow(
                context.getString(R.string.notifications_policy_omitted),
                delayLabel(policy.omittedAlertMinutes),
            )
            HorizontalDivider(color = tataMutedColor().copy(alpha = 0.18f))
            PolicyRow(context.getString(R.string.notifications_policy_summary), summary)
        }
        PolicyCard(
            context.getString(R.string.notifications_policy_emergency),
            R.raw.notifications_emergency,
            enabled,
            { editing = PolicyGroup.Emergency },
        ) {
            PolicyRow(
                context.getString(R.string.notifications_policy_contacts),
                context.getString(R.string.notifications_policy_contacts_count, contacts.size),
            )
            HorizontalDivider(color = tataMutedColor().copy(alpha = 0.18f))
            PolicyRow(
                context.getString(R.string.notifications_policy_escalation),
                delayLabel(policy.escalationMinutes),
            )
        }
    }
    editing?.let { group ->
        PolicyEditor(
            group,
            policy,
            contacts,
            saving = !enabled,
            saveMessage = saveMessage,
            onDismiss = { editing = null },
            onSave = onSave,
        )
    }
}

@Composable
private fun PolicyCard(
    title: String,
    icon: Int,
    enabled: Boolean,
    onEdit: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val edit = LocalContext.current.getString(R.string.notifications_edit)
    TataCard(
        Modifier.fillMaxWidth()
            .shadow(
                6.dp,
                RoundedCornerShape(18.dp),
                ambientColor = Color(0x141A2138),
                spotColor = Color(0x141A2138),
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onEdit)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TataSvgIcon(icon, Modifier.size(22.dp))
            Text(
                title,
                Modifier.weight(1f).padding(start = 7.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = tataTextColor(),
            )
            Text(
                edit,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Column(Modifier.padding(start = 12.dp, end = 4.dp), content = content)
    }
}

@Composable
private fun PolicyRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            Modifier.weight(1.3f),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = tataTextColor(),
        )
        Text(
            value,
            Modifier.weight(1f).padding(vertical = 4.dp),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            color = tataMutedColor(),
        )
        Text("›", Modifier.padding(start = 8.dp), fontSize = 13.sp, color = tataMutedColor())
    }
}

@Composable
private fun delayLabel(minutes: Int): String {
    val context = LocalContext.current
    return if (minutes % 60 == 0)
        context.getString(R.string.notifications_policy_after_hours, minutes / 60)
    else context.getString(R.string.notifications_policy_after_minutes, minutes)
}

private data class ContactDraft(val name: String, val phone: String) : Serializable

@Composable
private fun PolicyEditor(
    group: PolicyGroup,
    policy: NotificationPolicy,
    contacts: List<EmergencyContact>,
    saving: Boolean,
    saveMessage: NotificationMessage?,
    onDismiss: () -> Unit,
    onSave: (NotificationPolicy, List<EmergencyContact>) -> Unit,
) {
    val context = LocalContext.current
    var awaitingSave by rememberSaveable { mutableStateOf(false) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(saving, saveMessage) {
        if (awaitingSave && !saving && saveMessage != null) {
            awaitingSave = false
            if (saveMessage == NotificationMessage.Saved) onDismiss() else saveFailed = true
        }
    }
    var reminder by rememberSaveable { mutableStateOf(policy.reminderMinutes.toString()) }
    var pending by rememberSaveable { mutableStateOf(policy.pendingAlertMinutes.toString()) }
    var omitted by rememberSaveable { mutableStateOf(policy.omittedAlertMinutes.toString()) }
    var escalation by rememberSaveable { mutableStateOf(policy.escalationMinutes.toString()) }
    var summaryEnabled by rememberSaveable { mutableStateOf(policy.dailySummaryHour != null) }
    var summaryHour by rememberSaveable { mutableIntStateOf(policy.dailySummaryHour ?: 20) }
    var summaryMinute by rememberSaveable { mutableIntStateOf(policy.dailySummaryMinute ?: 0) }
    var drafts by rememberSaveable {
        mutableStateOf(ArrayList(contacts.map { ContactDraft(it.name, it.phone) }))
    }
    var error by rememberSaveable { mutableStateOf(false) }
    val title =
        context.getString(
            when (group) {
                PolicyGroup.Reminders -> R.string.notifications_policy_reminders
                PolicyGroup.Caregiver -> R.string.notifications_policy_caregiver
                PolicyGroup.Emergency -> R.string.notifications_policy_emergency
            }
        )
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(title) },
        text = {
            if (saving) Text(context.getString(R.string.notifications_loading))
            else
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (group) {
                        PolicyGroup.Reminders ->
                            MinutesField(
                                context.getString(R.string.notifications_policy_cadence),
                                reminder,
                            ) {
                                reminder = it
                            }
                        PolicyGroup.Caregiver -> {
                            MinutesField(
                                context.getString(R.string.notifications_policy_pending),
                                pending,
                            ) {
                                pending = it
                            }
                            MinutesField(
                                context.getString(R.string.notifications_policy_omitted),
                                omitted,
                            ) {
                                omitted = it
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    context.getString(R.string.notifications_policy_summary),
                                    Modifier.weight(1f),
                                )
                                Switch(summaryEnabled, { summaryEnabled = it })
                            }
                            if (summaryEnabled) {
                                TextButton(
                                    onClick = {
                                        TimePickerDialog(
                                                context,
                                                { _, hour, minute ->
                                                    summaryHour = hour
                                                    summaryMinute = minute
                                                },
                                                summaryHour,
                                                summaryMinute,
                                                DateFormat.is24HourFormat(context),
                                            )
                                            .show()
                                    }
                                ) {
                                    Text(
                                        LocalTime.of(summaryHour, summaryMinute)
                                            .format(
                                                DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                                                    .withLocale(
                                                        context.resources.configuration.locales[0]
                                                    )
                                            )
                                    )
                                }
                            }
                        }
                        PolicyGroup.Emergency -> {
                            MinutesField(
                                context.getString(R.string.notifications_policy_escalation),
                                escalation,
                            ) {
                                escalation = it
                            }
                            drafts.forEachIndexed { index, draft ->
                                OutlinedTextField(
                                    draft.name,
                                    { value ->
                                        drafts =
                                            ArrayList(drafts).apply {
                                                set(index, draft.copy(name = value))
                                            }
                                    },
                                    label = {
                                        Text(
                                            context.getString(
                                                R.string.notifications_policy_contact_name
                                            )
                                        )
                                    },
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    draft.phone,
                                    { value ->
                                        drafts =
                                            ArrayList(drafts).apply {
                                                set(index, draft.copy(phone = value))
                                            }
                                    },
                                    label = {
                                        Text(
                                            context.getString(
                                                R.string.notifications_policy_contact_phone
                                            )
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Phone),
                                )
                                TextButton(
                                    onClick = {
                                        drafts = ArrayList(drafts).apply { removeAt(index) }
                                    }
                                ) {
                                    Text(
                                        context.getString(
                                            R.string.notifications_policy_remove_contact,
                                            index + 1,
                                        )
                                    )
                                }
                            }
                            if (drafts.size < 5)
                                TextButton(
                                    onClick = {
                                        drafts =
                                            ArrayList(drafts).apply { add(ContactDraft("", "")) }
                                    }
                                ) {
                                    Text(
                                        context.getString(R.string.notifications_policy_add_contact)
                                    )
                                }
                        }
                    }
                    if (saveFailed)
                        Text(
                            context.getString(R.string.accessibility_error_generic),
                            color = MaterialTheme.colorScheme.error,
                        )
                    if (error)
                        Text(
                            context.getString(R.string.notifications_policy_invalid),
                            color = MaterialTheme.colorScheme.error,
                        )
                }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    val updated =
                        runCatching {
                                val changed =
                                    NotificationPolicy(
                                        reminder.toInt(),
                                        pending.toInt(),
                                        omitted.toInt(),
                                        escalation.toInt(),
                                        summaryHour.takeIf { summaryEnabled },
                                        summaryMinute.takeIf { summaryEnabled },
                                    )
                                val recipients =
                                    drafts.map { EmergencyContact.fromInput(it.name, it.phone) }
                                require(
                                    recipients.map { it.phone.removePrefix("+") }.distinct().size ==
                                        recipients.size
                                )
                                changed to recipients
                            }
                            .getOrNull()
                    if (updated == null) error = true
                    else {
                        awaitingSave = true
                        saveFailed = false
                        onSave(updated.first, updated.second)
                    }
                },
            ) {
                Text(context.getString(R.string.notifications_policy_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text(context.getString(R.string.notifications_policy_cancel))
            }
        },
    )
}

@Composable
private fun MinutesField(label: String, value: String, onChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(
        value,
        onChange,
        label = { Text(label) },
        suffix = { Text(context.getString(R.string.notifications_policy_minutes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
