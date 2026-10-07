package com.vitahealth.tata.inventory.presentation.inventory

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.inventory.R
import com.vitahealth.tata.inventory.application.readmodels.InventoryStockReadModel
import com.vitahealth.tata.inventory.domain.model.InventoryBatch
import com.vitahealth.tata.inventory.domain.model.StockStatus
import com.vitahealth.tata.shared.design.components.TataButton
import com.vitahealth.tata.shared.design.components.TataCard
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataCream
import com.vitahealth.tata.shared.design.theme.TataError
import com.vitahealth.tata.shared.design.theme.TataMint
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataSurface
import com.vitahealth.tata.shared.design.theme.TataText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val AvailableGreen = Color(0xFF2E754A)

private val batchDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())

@Composable
fun InventoryRoute(
    factory: InventoryViewModel.Factory,
    modifier: Modifier = Modifier,
) {
    val viewModel: InventoryViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    InventoryScreen(
        state = state,
        onInitialQuantityChange = viewModel::onInitialQuantityChange,
        onThresholdChange = viewModel::onThresholdChange,
        onDefineInitial = viewModel::defineInitialInventory,
        onReplenishmentChange = viewModel::onReplenishmentQuantityChange,
        onSaveReplenishment = viewModel::saveReplenishment,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    onInitialQuantityChange: (String) -> Unit,
    onThresholdChange: (String) -> Unit,
    onDefineInitial: () -> Unit,
    onReplenishmentChange: (String) -> Unit,
    onSaveReplenishment: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.inventory_title),
            color = TataText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.inventory_subtitle),
            color = TataMuted,
        )

        when (state) {
            is InventoryUiState.Loading -> LoadingBody()
            is InventoryUiState.NotInitialized -> NotInitializedBody(
                state = state,
                onInitialQuantityChange = onInitialQuantityChange,
                onThresholdChange = onThresholdChange,
                onDefineInitial = onDefineInitial,
            )
            is InventoryUiState.Ready -> ReadyBody(
                state = state,
                onReplenishmentChange = onReplenishmentChange,
                onSaveReplenishment = onSaveReplenishment,
            )
            is InventoryUiState.Error -> ErrorBody(code = state.code, onRetry = onRetry)
        }
    }
}

@Composable
private fun LoadingBody() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ReadyBody(
    state: InventoryUiState.Ready,
    onReplenishmentChange: (String) -> Unit,
    onSaveReplenishment: () -> Unit,
) {
    StockCard(state = state)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.inventory_replenishment_section),
            color = TataText,
            fontWeight = FontWeight.SemiBold,
        )
        InventoryQuantityField(
            label = stringResource(R.string.inventory_quantity_received_label),
            value = state.replenishmentInput,
            onValueChange = onReplenishmentChange,
            isError = state.replenishmentErrorCode == "INVALID_QUANTITY",
            enabled = !state.submitting,
            errorLabel = stringResource(R.string.inventory_invalid_quantity_label),
        )
        state.replenishmentErrorCode?.let { code ->
            Text(text = stringResource(inventoryErrorMessageRes(code)), color = TataError)
        }
        if (state.justRegistered) {
            Text(text = stringResource(R.string.inventory_replenishment_saved), color = AvailableGreen)
        }
        TataButton(
            text = stringResource(
                if (state.submitting) R.string.inventory_saving else R.string.inventory_save_replenishment,
            ),
            onClick = onSaveReplenishment,
            enabled = !state.submitting,
        )
    }

    state.stock.lastReplenishment?.let { batch ->
        LastReplenishmentCard(batch = batch, unit = state.unit)
    }
}

@Composable
private fun StockCard(state: InventoryUiState.Ready) {
    val stock = state.stock
    val isLow = stock.status == StockStatus.LOW
    val statusText = stringResource(
        if (isLow) R.string.inventory_status_low else R.string.inventory_status_available,
    )
    val statusColor = if (isLow) TataError else AvailableGreen
    val daysText = stock.daysRemaining
        ?.let { stringResource(R.string.inventory_days_estimate, it) }
        ?: stringResource(R.string.inventory_days_no_estimate)

    TataCard(containerColor = TataCream, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = state.medicationName, color = TataText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = statusText, color = statusColor, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (state.unit.isBlank()) {
                stringResource(R.string.inventory_units_remaining_no_unit, stock.remainingStock)
            } else {
                stringResource(R.string.inventory_units_remaining, stock.remainingStock, state.unit)
            },
            color = TataText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(text = daysText, color = TataMuted)
    }
}

@Composable
private fun LastReplenishmentCard(batch: InventoryBatch, unit: String) {
    TataCard(containerColor = TataMint, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.inventory_last_replenishment), color = TataText, fontWeight = FontWeight.SemiBold)
            Text(text = stringResource(R.string.inventory_registered_badge), color = AvailableGreen)
        }
        Spacer(Modifier.height(4.dp))
        Text(text = batchDateFormatter.format(batch.registeredAt), color = TataText, fontWeight = FontWeight.Bold)
        Text(
            text = if (unit.isBlank()) {
                stringResource(R.string.inventory_batch_summary_no_unit, batch.quantity)
            } else {
                stringResource(R.string.inventory_batch_summary, batch.quantity, unit)
            },
            color = TataMuted,
        )
    }
}

@Composable
private fun NotInitializedBody(
    state: InventoryUiState.NotInitialized,
    onInitialQuantityChange: (String) -> Unit,
    onThresholdChange: (String) -> Unit,
    onDefineInitial: () -> Unit,
) {
    Text(text = stringResource(R.string.inventory_not_initialized), color = TataMuted)

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
        Text(text = stringResource(inventoryErrorMessageRes(code)), color = TataError)
    }
    TataButton(
        text = stringResource(
            if (state.submitting) R.string.inventory_saving else R.string.inventory_define_initial,
        ),
        onClick = onDefineInitial,
        enabled = !state.submitting,
    )
}

@Composable
private fun ErrorBody(code: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(inventoryErrorMessageRes(code)),
            color = TataError,
            textAlign = TextAlign.Center,
        )
        TataButton(text = stringResource(R.string.inventory_retry), onClick = onRetry)
    }
}

/**
 * Numeric field that turns red and swaps its label to [errorLabel] when [isError] (e.g. the
 * "Cantidad inválida" state in frame 294-3255). A local variant because the shared
 * [com.vitahealth.tata.shared.design.components.TataFormField] has no error state and lives in
 * another module.
 */
@Composable
private fun InventoryQuantityField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    errorLabel: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (isError && errorLabel != null) errorLabel else label,
            color = if (isError) TataError else TataMuted,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = if (isError) TataError else TataBorder,
                unfocusedBorderColor = if (isError) TataError else TataBorder,
                errorBorderColor = TataError,
                focusedTextColor = TataText,
                unfocusedTextColor = TataText,
            ),
        )
    }
}

/** Maps a stable backend/app error code to a localized message resource. */
@StringRes
internal fun inventoryErrorMessageRes(code: String?): Int = when (code) {
    "INVALID_QUANTITY" -> R.string.inventory_error_invalid_quantity
    "INVALID_THRESHOLD" -> R.string.inventory_error_invalid_threshold
    "INVALID_MEDICATION_REFERENCE" -> R.string.inventory_error_invalid_medication_reference
    "INVENTORY_NOT_FOUND" -> R.string.inventory_error_inventory_not_found
    "INVENTORY_ALREADY_EXISTS" -> R.string.inventory_error_inventory_already_exists
    "CONCURRENT_UPDATE" -> R.string.inventory_error_concurrent_update
    "REQUEST_FAILED" -> R.string.inventory_error_request_failed
    "NETWORK_UNAVAILABLE" -> R.string.inventory_error_network_unavailable
    "INVALID_RESPONSE" -> R.string.inventory_error_invalid_response
    else -> R.string.inventory_error_unknown
}

// --- Previews of the Figma states ---

private fun sampleStock(remaining: Int, status: StockStatus) = InventoryStockReadModel(
    medicationId = "med-1",
    remainingStock = remaining,
    replenishmentThreshold = 5,
    status = status,
    batches = listOf(
        InventoryBatch(id = "b1", quantity = 30, registeredAt = Instant.parse("2026-09-02T10:00:00Z")),
    ),
    createdAt = Instant.parse("2026-08-01T10:00:00Z"),
    updatedAt = Instant.parse("2026-09-02T10:00:00Z"),
    daysRemaining = null,
)

private val noop: () -> Unit = {}
private val noopStr: (String) -> Unit = {}

@Preview(name = "Low stock", showBackground = true)
@Composable
private fun InventoryLowStockPreview() {
    InventoryScreen(
        state = InventoryUiState.Ready(
            stock = sampleStock(5, StockStatus.LOW),
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
        ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Stock available", showBackground = true)
@Composable
private fun InventoryAvailablePreview() {
    InventoryScreen(
        state = InventoryUiState.Ready(
            stock = sampleStock(35, StockStatus.AVAILABLE),
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
            replenishmentInput = "30",
        ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Invalid quantity", showBackground = true)
@Composable
private fun InventoryInvalidQuantityPreview() {
    InventoryScreen(
        state = InventoryUiState.Ready(
            stock = sampleStock(5, StockStatus.LOW),
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
            replenishmentErrorCode = "INVALID_QUANTITY",
        ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Not initialized (404)", showBackground = true)
@Composable
private fun InventoryNotInitializedPreview() {
    InventoryScreen(
        state = InventoryUiState.NotInitialized(
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
        ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "Stock bajo (es-419)", locale = "b+es+419", showBackground = true)
@Composable
private fun InventoryLowStockEsPreview() {
    InventoryScreen(
        state = InventoryUiState.Ready(
            stock = sampleStock(5, StockStatus.LOW),
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
        ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}

@Preview(name = "No inicializado (es-419)", locale = "b+es+419", showBackground = true)
@Composable
private fun InventoryNotInitializedEsPreview() {
    InventoryScreen(
        state = InventoryUiState.NotInitialized(
            medicationName = "Losartán 50 mg",
            unit = "comprimidos",
        ),
        onInitialQuantityChange = noopStr,
        onThresholdChange = noopStr,
        onDefineInitial = noop,
        onReplenishmentChange = noopStr,
        onSaveReplenishment = noop,
        onRetry = noop,
    )
}
