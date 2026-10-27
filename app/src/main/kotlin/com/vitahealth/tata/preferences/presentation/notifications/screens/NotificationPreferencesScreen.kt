package com.vitahealth.tata.preferences.presentation.notifications.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.preferences.presentation.notifications.ui.ChannelsCard
import com.vitahealth.tata.preferences.presentation.notifications.ui.MessageBanner
import com.vitahealth.tata.preferences.presentation.notifications.ui.NotificationPolicyCards
import com.vitahealth.tata.preferences.presentation.notifications.ui.QuietHoursCard
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationMessage
import com.vitahealth.tata.preferences.presentation.notifications.viewmodel.NotificationPreferencesUiState
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

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
    onPolicyChange: (NotificationPolicy, List<EmergencyContact>) -> Unit = { _, _ -> },
    onTabSelected: ((CaregiverTab) -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        containerColor = TataSurface,
        bottomBar = {
            Column {
                if (state.preferences != null)
                    state.message?.let {
                        Box(Modifier.padding(horizontal = 22.dp, vertical = 4.dp)) {
                            MessageBanner(it, state.messageIsError)
                        }
                    }
                onTabSelected?.let { CaregiverTabBar(CaregiverTab.More, it) }
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier.padding(padding)
                    .fillMaxSize()
                    .background(TataSurface)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val backLabel = stringResource(R.string.accessibility_back)
                Box(
                    modifier =
                        Modifier.size(48.dp)
                            .clickable(role = Role.Button, onClick = onBack)
                            .semantics { contentDescription = backLabel },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "‹", fontSize = 28.sp, color = TataNavy)
                }
                Text(
                    text = stringResource(R.string.notifications_title),
                    fontFamily = FontFamily(Font(R.font.tata_serif)),
                    fontSize = 21.sp,
                    lineHeight = 26.sp,
                    color = tataTextColor(),
                )
            }
            Spacer(Modifier.height(12.dp))

            val preferences = state.preferences
            when {
                state.isLoading ->
                    TataCard(Modifier.fillMaxWidth(), TataMint) {
                        Text(
                            stringResource(R.string.notifications_loading),
                            color = tataTextColor(),
                        )
                    }

                preferences == null -> {
                    state.message?.let { MessageBanner(it, state.messageIsError) }
                    Spacer(Modifier.height(12.dp))
                    TataButton(
                        text = stringResource(R.string.notifications_retry),
                        onClick = onRetry,
                    )
                }

                else -> {
                    preferences.policy?.let { policy ->
                        NotificationPolicyCards(
                            policy,
                            preferences.emergencyContacts,
                            !state.isSaving,
                            state.message,
                            onPolicyChange,
                        )
                        Spacer(Modifier.height(14.dp))
                    }
                    QuietHoursCard(
                        preferences = preferences,
                        enabled = !state.isSaving,
                        onEnabledChange = onQuietHoursEnabledChange,
                        onStartChange = onQuietHoursStartChange,
                        onEndChange = onQuietHoursEndChange,
                    )
                    Spacer(Modifier.height(14.dp))
                    ChannelsCard(
                        preferences = preferences,
                        enabled = !state.isSaving,
                        onChannelChange = onChannelChange,
                    )
                }
            }
        }
    }
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
