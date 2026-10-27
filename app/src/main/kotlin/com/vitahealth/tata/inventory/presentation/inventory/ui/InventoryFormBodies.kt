package com.vitahealth.tata.inventory.presentation.inventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.R
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import com.vitahealth.tata.inventory.presentation.inventory.mappers.inventoryErrorMessageRes
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataErrorSurface
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataSuccess

@Composable
internal fun LoadingBody() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/** Keeps stock feedback and the replenishment form within a single screen state. */
@Composable
internal fun ReadyBody(
    state: InventoryUiState.Ready,
    onReplenishmentChange: (String) -> Unit,
    onSaveReplenishment: () -> Unit,
    onLotChange: (String) -> Unit,
) {
    val quantityFocus = remember { FocusRequester() }
    var showInitialInfo by remember { mutableStateOf(false) }
    StockCard(state)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = { showInitialInfo = true }) {
            InventoryText(
                stringResource(R.string.inventory_define_initial),
                12,
                tataAccentColor(TataNavy),
                FontWeight.SemiBold,
            )
        }
    }
    InventoryAction(
        stringResource(R.string.inventory_replenishment_section),
        onClick = { quantityFocus.requestFocus() },
        enabled = !state.submitting,
    )
    Spacer(Modifier.height(22.dp))
    InventoryQuantityField(
        label = stringResource(R.string.inventory_quantity_received_label),
        value = state.replenishmentInput,
        onValueChange = onReplenishmentChange,
        isError = state.replenishmentErrorCode == "INVALID_QUANTITY",
        enabled = !state.submitting,
        errorLabel = stringResource(R.string.inventory_invalid_quantity_label),
        focusRequester = quantityFocus,
    )
    Spacer(Modifier.height(2.dp))
    TataFormField(
        label = stringResource(R.string.inventory_lot_label),
        value = state.lotInput,
        onValueChange = onLotChange,
        enabled = !state.submitting,
        softSurface = true,
    )
    Spacer(Modifier.height(20.dp))
    InventoryAction(
        stringResource(
            if (state.submitting) R.string.inventory_saving
            else R.string.inventory_save_replenishment
        ),
        onSaveReplenishment,
        !state.submitting,
        secondary = true,
    )
    Spacer(Modifier.height(10.dp))
    if (state.justRegistered) {
        TataCard(
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            containerColor = tataTonalColor(Color(0xFFE8F5EC)),
        ) {
            InventoryText(
                stringResource(R.string.inventory_replenishment_saved),
                11,
                tataAccentColor(TataSuccess),
                FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(10.dp))
    }
    state.stock.lastReplenishment?.let { LastReplenishmentCard(it, state.unit) }
    state.replenishmentErrorCode
        ?.takeUnless { it == "INVALID_QUANTITY" }
        ?.let { code ->
            Spacer(Modifier.height(12.dp))
            TataCard(containerColor = tataTonalColor(TataErrorSurface)) {
                InventoryText(stringResource(inventoryErrorMessageRes(code)), 11, inventoryErrorColor())
            }
        }
    if (showInitialInfo)
        AlertDialog(
            onDismissRequest = { showInitialInfo = false },
            title = { Text(stringResource(R.string.inventory_define_initial)) },
            text = { Text(stringResource(R.string.inventory_initialized_info)) },
            confirmButton = {
                TextButton(onClick = { showInitialInfo = false }) {
                    Text(stringResource(R.string.inventory_close))
                }
            },
        )
}

/** Shows the initial-stock form only when the query reports no inventory. */
@Composable
internal fun NotInitializedBody(
    state: InventoryUiState.NotInitialized,
    onInitialQuantityChange: (String) -> Unit,
    onThresholdChange: (String) -> Unit,
    onDefineInitial: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = stringResource(R.string.inventory_not_initialized), color = tataMutedColor())

        InventoryQuantityField(
            label = stringResource(R.string.inventory_initial_quantity_label),
            value = state.initialQuantityInput,
            onValueChange = onInitialQuantityChange,
            isError = state.formErrorCode == "INVALID_QUANTITY",
            enabled = !state.submitting,
            errorLabel = stringResource(R.string.inventory_invalid_quantity_label),
        )
        InventoryQuantityField(
            label = stringResource(R.string.inventory_threshold_label),
            value = state.thresholdInput,
            onValueChange = onThresholdChange,
            isError = state.formErrorCode == "INVALID_THRESHOLD",
            enabled = !state.submitting,
            errorLabel = stringResource(R.string.inventory_invalid_threshold_label),
        )
        state.formErrorCode?.let { code ->
            Text(text = stringResource(inventoryErrorMessageRes(code)), color = inventoryErrorColor())
        }
        TataButton(
            text =
                stringResource(
                    if (state.submitting) R.string.inventory_saving
                    else R.string.inventory_define_initial
                ),
            onClick = onDefineInitial,
            enabled = !state.submitting,
        )
    }
}

/** Restricts retry, sign-in and back actions according to the access error. */
@Composable
internal fun ErrorBody(
    state: InventoryUiState.Error,
    onRetry: () -> Unit,
    onSignIn: (() -> Unit)?,
    onBack: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(inventoryErrorMessageRes(state.code)),
            color = inventoryErrorColor(),
            textAlign = TextAlign.Center,
        )
        val recoverSession = state.requiresSignIn && onSignIn != null
        val leaveDeniedScreen = state.accessDenied && onBack != null
        TataButton(
            text =
                stringResource(
                    when {
                        recoverSession -> R.string.inventory_sign_in
                        leaveDeniedScreen -> R.string.inventory_back
                        else -> R.string.inventory_retry
                    }
                ),
            onClick =
                when {
                    recoverSession -> requireNotNull(onSignIn)
                    leaveDeniedScreen -> requireNotNull(onBack)
                    else -> onRetry
                },
        )
    }
}
