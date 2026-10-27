package com.vitahealth.tata.treatment.presentation.medication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataWarning
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.treatment.domain.model.entities.Medication
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationDraft
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MedicationManagementMessage

internal val StatusActive = Color(0xFF296345)

@Composable
internal fun Header(olderAdultName: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val backLabel = stringResource(R.string.medications_back)
        Box(
            modifier =
                Modifier.size(48.dp).clickable(role = Role.Button, onClick = onBack).semantics {
                    contentDescription = backLabel
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "‹", fontSize = 28.sp, color = tataAccentColor())
        }
        Column {
            Text(
                text = stringResource(R.string.medications_title),
                fontFamily = FontFamily(Font(R.font.tata_serif)),
                fontSize = 26.sp,
                color = tataTextColor(),
            )
            Text(
                text =
                    stringResource(
                        R.string.medications_subtitle,
                        olderAdultName.substringBefore(" "),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = tataMutedColor(),
            )
        }
    }
}

@Composable
internal fun MedicationCard(
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
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = tataTextColor(),
                )
                Text(
                    text = medication.presentation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tataMutedColor(),
                )
            }
            Text(
                text =
                    stringResource(
                        if (medication.active) R.string.medications_status_active
                        else R.string.medications_status_inactive
                    ),
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
            TataButton(
                text = stringResource(R.string.medications_save),
                onClick = onSaveEdit,
                enabled = enabled,
            )
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
                color = tataMutedColor(),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
internal fun AccessDenied(onBack: () -> Unit) {
    TataCard(Modifier.fillMaxWidth(), TataErrorSurface) {
        Text(
            text = stringResource(R.string.medications_denied_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = tataTextColor(),
        )
        Text(
            text = stringResource(R.string.medications_denied_body),
            style = MaterialTheme.typography.bodyMedium,
            color = tataMutedColor(),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    Spacer(Modifier.height(16.dp))
    TataButton(text = stringResource(R.string.medications_denied_back), onClick = onBack)
}

@Composable
internal fun Banner(message: MedicationManagementMessage, isError: Boolean) {
    val (title, body) =
        when (message) {
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
    val container =
        when {
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
