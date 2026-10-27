package com.vitahealth.tata.intake.presentation.detail.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.R
import com.vitahealth.tata.intake.application.readmodels.DoseDetailReadModel
import com.vitahealth.tata.intake.application.readmodels.NextDoseReadModel
import com.vitahealth.tata.intake.domain.model.valueobjects.DoseStatus
import com.vitahealth.tata.intake.presentation.detail.ui.DoseContent
import com.vitahealth.tata.intake.presentation.detail.ui.detailSerif
import com.vitahealth.tata.intake.presentation.detail.viewmodel.ConfirmationOutcome
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailUiState
import com.vitahealth.tata.intake.presentation.detail.viewmodel.DoseDetailViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTabBar
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarningSurface
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun DoseDetailScreen(
    state: DoseDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    onHome: () -> Unit = onBack,
    onAdultTab: ((AdultTab, String) -> Unit)? = null,
) {
    if (
        state is DoseDetailUiState.Content &&
            (state.confirmationSucceeded || state.outcome == ConfirmationOutcome.OMISSION_PRESERVED)
    ) {
        DoseOutcomeScreen(
            state,
            onHome,
            modifier,
            onTab =
                onAdultTab?.let { callback -> { tab -> callback(tab, state.dose.olderAdultId) } },
        )
        return
    }
    Column(
        modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(listOf(Color.White, TataSurface, Color(0xFFFCFBFF)))
            )
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(28.dp))
            val backLabel = stringResource(R.string.detail_back)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp).semantics { contentDescription = backLabel },
                ) {
                    Text("‹", color = TataNavy, fontSize = 24.sp)
                }
                Text(
                    stringResource(R.string.detail_title),
                    fontFamily = detailSerif,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    color = TataText,
                )
            }
            Spacer(Modifier.height(22.dp))
            when (state) {
                DoseDetailUiState.Loading ->
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.detail_loading), color = TataMuted)
                    }
                is DoseDetailUiState.Error ->
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.detail_error_title),
                            fontWeight = FontWeight.Bold,
                            color = TataText,
                        )
                        Text(
                            state.message,
                            color = TataMuted,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        TataButton(
                            stringResource(R.string.home_retry),
                            onRetry,
                            Modifier.padding(top = 16.dp),
                        )
                    }
                is DoseDetailUiState.Content -> {
                    DoseContent(state.dose, state.nextDose)
                    state.confirmationMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                    if (state.confirmationUnavailable) {
                        TataButton(stringResource(R.string.home_retry), onRetry)
                    } else if (state.dose.status == DoseStatus.PENDING) {
                        TataButton(
                            stringResource(
                                if (state.confirming) R.string.detail_confirming
                                else R.string.detail_confirm
                            ),
                            onConfirm,
                            enabled = !state.confirming,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        val owner = (state as? DoseDetailUiState.Content)?.dose?.olderAdultId
        AdultTabBar(
            selected = AdultTab.Medications,
            availableTabs =
                if (owner != null && onAdultTab != null)
                    setOf(AdultTab.Home, AdultTab.Medications, AdultTab.Agenda, AdultTab.Notes)
                else emptySet(),
            onSelect = { tab -> if (owner != null) onAdultTab?.invoke(tab, owner) },
        )
    }
}
