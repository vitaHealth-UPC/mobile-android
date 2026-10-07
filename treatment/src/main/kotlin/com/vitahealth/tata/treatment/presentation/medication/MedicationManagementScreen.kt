package com.vitahealth.tata.treatment.presentation.medication

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
import com.vitahealth.tata.treatment.R
import com.vitahealth.tata.treatment.domain.model.Medication
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataButtonStyle
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataErrorSurface
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import com.vitahealth.tata.shared.design.theme.TataTheme
import com.vitahealth.tata.shared.design.theme.TataWarning
import com.vitahealth.tata.shared.design.theme.TataWarningSurface

private val StatusActive = Color(0xFF296345)

@Composable
fun MedicationManagementRoute(
    factory: MedicationManagementViewModel.Factory,
    onBack: () -> Unit,
    onAddMedication: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MedicationManagementViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    MedicationManagementScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onAddMedication = onAddMedication,
        onEdit = viewModel::onEdit,
        onDraftNameChange = viewModel::onDraftNameChange,
        onDraftPresentationChange = viewModel::onDraftPresentationChange,
        onCancelEdit = viewModel::onCancelEdit,
        onSaveEdit = viewModel::onSaveEdit,
        onDeactivateRequest = viewModel::onDeactivateRequest,
        onDeactivateCancel = viewModel::onDeactivateCancel,
        onDeactivateConfirm = viewModel::onDeactivateConfirm,
        modifier = modifier,
    )
}

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
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 28.dp),
    ) {
        Header(olderAdultName = state.olderAdultName, onBack = onBack)
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> TataCard(Modifier.fillMaxWidth(), TataMint) {
                Text(stringResource(R.string.medications_loading), color = TataText)
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
                            color = TataMuted,
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
            title = { Text(stringResource(R.string.medications_deactivate_title, medication.name)) },
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

@Composable
private fun Header(olderAdultName: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val backLabel = stringResource(R.string.medications_back)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = backLabel },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "‹", fontSize = 28.sp, color = TataNavy)
        }
        Column {
            Text(
                text = stringResource(R.string.medications_title),
                fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
                fontSize = 26.sp,
                color = TataText,
            )
            Text(
                text = stringResource(R.string.medications_subtitle, olderAdultName.substringBefore(" ")),
                style = MaterialTheme.typography.bodySmall,
                color = TataMuted,
            )
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    draft: MedicationDraft?,
    enabled: Boolean,
    onEdit: () -> Unit,
    onDraftNameChange: (String) -> Unit,
    onDraftPresentationChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    onDeactivate: () -> Unit,
) {
    TataCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TataText,
                )
                Text(
                    text = medication.presentation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TataMuted,
                )
            }
            Text(
                text = stringResource(if (medication.active) R.string.medications_status_active else R.string.medications_status_inactive),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (medication.active) StatusActive else TataWarning,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        if (draft != null) {
            Spacer(Modifier.height(12.dp))
            TataFormField(
                label = stringResource(R.string.medications_field_name),
                value = draft.name,
                onValueChange = onDraftNameChange,
                enabled = enabled,
            )
            Spacer(Modifier.height(8.dp))
            TataFormField(
                label = stringResource(R.string.medications_field_presentation),
                value = draft.presentation,
                onValueChange = onDraftPresentationChange,
                enabled = enabled,
            )
            Spacer(Modifier.height(12.dp))
            TataButton(text = stringResource(R.string.medications_save), onClick = onSaveEdit, enabled = enabled)
            Spacer(Modifier.height(8.dp))
            TataButton(
                text = stringResource(R.string.medications_cancel),
                onClick = onCancelEdit,
                enabled = enabled,
                style = TataButtonStyle.Secondary,
            )
        } else if (medication.active) {
            Spacer(Modifier.height(12.dp))
            TataButton(
                text = stringResource(R.string.medications_edit),
                onClick = onEdit,
                enabled = enabled,
                style = TataButtonStyle.Secondary,
            )
            TextButton(
                onClick = onDeactivate,
                enabled = enabled,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.medications_deactivate),
                    color = TataError,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Text(
                text = stringResource(R.string.medications_inactive_note),
                style = MaterialTheme.typography.bodySmall,
                color = TataMuted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun AccessDenied(onBack: () -> Unit) {
    TataCard(Modifier.fillMaxWidth(), TataErrorSurface) {
        Text(
            text = stringResource(R.string.medications_denied_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TataText,
        )
        Text(
            text = stringResource(R.string.medications_denied_body),
            style = MaterialTheme.typography.bodyMedium,
            color = TataMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    Spacer(Modifier.height(16.dp))
    TataButton(text = stringResource(R.string.medications_denied_back), onClick = onBack)
}

@Composable
private fun Banner(message: MedicationManagementMessage, isError: Boolean) {
    val (title, body) = when (message) {
        MedicationManagementMessage.Updated ->
            R.string.medications_updated_title to R.string.medications_updated_body
        MedicationManagementMessage.Deactivated ->
            R.string.medications_deactivated_title to R.string.medications_deactivated_body
        MedicationManagementMessage.ErrorRequiredFields ->
            R.string.medications_error_title to R.string.medications_error_required
        MedicationManagementMessage.ErrorInactive ->
            R.string.medications_error_title to R.string.medications_error_inactive
        MedicationManagementMessage.ErrorNotFound ->
            R.string.medications_error_title to R.string.medications_error_not_found
        MedicationManagementMessage.ErrorOffline ->
            R.string.medications_error_title to R.string.medications_error_offline
        MedicationManagementMessage.ErrorGeneric ->
            R.string.medications_error_title to R.string.medications_error_generic
    }
    val container = when {
        isError -> TataErrorSurface
        message == MedicationManagementMessage.Deactivated -> TataCream
        else -> TataMint
    }
    TataCard(modifier = Modifier.fillMaxWidth(), containerColor = container) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) TataError else StatusActive,
        )
        Text(
            text = stringResource(body),
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) TataError else TataMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MedicationManagementScreenPreview() {
    TataTheme {
        MedicationManagementScreen(
            state = MedicationManagementUiState(
                olderAdultName = "Rosa Vargas",
                isLoading = false,
                medications = listOf(
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
