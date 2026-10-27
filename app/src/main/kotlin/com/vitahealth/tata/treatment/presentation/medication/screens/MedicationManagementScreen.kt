package com.vitahealth.tata.treatment.presentation.medication.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButtonStyle
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMint
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarning
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarningSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.presentation.medication.ui.AccessDenied
import com.vitahealth.tata.treatment.presentation.medication.ui.Banner
import com.vitahealth.tata.treatment.presentation.medication.ui.Header
import com.vitahealth.tata.treatment.presentation.medication.ui.MedicationCard
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationDraft
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementMessage
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementUiState


@Composable
fun MedicationManagementScreen(
    state: MedicationManagementUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onAddMedication: () -> Unit,
    onEdit: (Medication) -> Unit,
    onDraftNameChange: (String) -> Unit,
    onDraftPresentationChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onDeactivateRequest: (Medication) -> Unit,
    onDeactivateCancel: () -> Unit,
    onDeactivateConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(tataBackgroundColor())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 28.dp)
    ) {
        Header(olderAdultName = state.olderAdultName, onBack = onBack)
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading ->
                TataCard(Modifier.fillMaxWidth(), TataMint) {
                    Text(stringResource(R.string.medications_loading), color = tataTextColor())
                }

            state.accessDenied -> AccessDenied(onBack = onBack)

            state.loadFailed -> {
                state.message?.let { Banner(it, state.messageIsError) }
                Spacer(Modifier.height(12.dp))
                TataButton(text = stringResource(R.string.medications_retry), onClick = onRetry)
            }

            else -> {
                if (state.medications.isEmpty()) {
                    TataCard(Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.medications_empty),
                            color = tataMutedColor(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                state.medications.forEach { medication ->
                    val draft = state.draft?.takeIf { it.medicationId == medication.id }
                    MedicationCard(
                        medication = medication,
                        draft = draft,
                        enabled = !state.isSaving,
                        onEdit = { onEdit(medication) },
                        onDraftNameChange = onDraftNameChange,
                        onDraftPresentationChange = onDraftPresentationChange,
                        onCancelEdit = onCancelEdit,
                        onSaveEdit = onSaveEdit,
                        onDeactivate = { onDeactivateRequest(medication) },
                    )
                    Spacer(Modifier.height(14.dp))
                }

                TataCard(Modifier.fillMaxWidth(), TataWarningSurface) {
                    Text(
                        text = stringResource(R.string.medications_history_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TataWarning,
                    )
                    Text(
                        text = stringResource(R.string.medications_history_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = TataWarning,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Spacer(Modifier.height(14.dp))
                TataButton(
                    text = stringResource(R.string.medications_add),
                    onClick = onAddMedication,
                    style = TataButtonStyle.Secondary,
                )
                state.message?.let {
                    Spacer(Modifier.height(16.dp))
                    Banner(it, state.messageIsError)
                }
            }
        }
    }

    state.deactivating?.let { medication ->
        AlertDialog(
            onDismissRequest = onDeactivateCancel,
            title = {
                Text(stringResource(R.string.medications_deactivate_title, medication.name))
            },
            text = { Text(stringResource(R.string.medications_deactivate_body)) },
            confirmButton = {
                TextButton(onClick = onDeactivateConfirm) {
                    Text(stringResource(R.string.medications_deactivate_confirm), color = TataError)
                }
            },
            dismissButton = {
                TextButton(onClick = onDeactivateCancel) {
                    Text(stringResource(R.string.medications_cancel))
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationManagementScreenPreview() {
    TataTheme {
        MedicationManagementScreen(
            state =
                MedicationManagementUiState(
                    olderAdultName = "Rosa Vargas",
                    isLoading = false,
                    medications =
                        listOf(
                            Medication("1", "adult", "Losartán", "50 mg, comprimido", true),
                            Medication("2", "adult", "Metformina", "850 mg, comprimido", false),
                        ),
                    message = MedicationManagementMessage.Updated,
                ),
            onBack = {},
            onRetry = {},
            onAddMedication = {},
            onEdit = {},
            onDraftNameChange = {},
            onDraftPresentationChange = {},
            onCancelEdit = {},
            onSaveEdit = {},
            onDeactivateRequest = {},
            onDeactivateCancel = {},
            onDeactivateConfirm = {},
        )
    }
}
