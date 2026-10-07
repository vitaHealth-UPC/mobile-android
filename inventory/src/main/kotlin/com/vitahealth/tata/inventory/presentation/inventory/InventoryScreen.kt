package com.vitahealth.tata.inventory.presentation.inventory

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.vitahealth.tata.shared.design.components.TataFormField
import com.vitahealth.tata.shared.design.theme.TataDeepNavy
import com.vitahealth.tata.shared.design.theme.TataNavy
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.tataPrototypeTopPadding
import com.vitahealth.tata.shared.design.theme.tataPrototypeShadow
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
        onLotChange = viewModel::onLotChange,
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
    onLotChange: (String) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TataSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp).padding(top = tataPrototypeTopPadding(), bottom = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.inventory_title),
            color = TataText,
            fontSize = 29.sp, lineHeight = 38.sp,
            fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),
            modifier = Modifier.padding(start = 2.dp),
        )
        Text(
            text = stringResource(R.string.inventory_subtitle),
            color = TataMuted, fontSize = 12.sp, lineHeight = 16.sp,
            fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
            modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 20.dp),
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
                onLotChange = onLotChange,
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
    onLotChange: (String) -> Unit,
) {
    val quantityFocus = remember { FocusRequester() }
    StockCard(state)
    Spacer(Modifier.height(28.dp))
    InventoryAction(stringResource(R.string.inventory_replenishment_section),
        onClick = { quantityFocus.requestFocus() }, enabled = !state.submitting)
    Spacer(Modifier.height(22.dp))
    InventoryQuantityField(label = stringResource(R.string.inventory_quantity_received_label),
        value = state.replenishmentInput, onValueChange = onReplenishmentChange,
        isError = state.replenishmentErrorCode == "INVALID_QUANTITY", enabled = !state.submitting,
        errorLabel = stringResource(R.string.inventory_invalid_quantity_label), focusRequester = quantityFocus)
    Spacer(Modifier.height(2.dp))
    TataFormField(label = stringResource(R.string.inventory_lot_label), value = state.lotInput,
        onValueChange = onLotChange, enabled = !state.submitting, softSurface = true)
    Spacer(Modifier.height(20.dp))
    InventoryAction(stringResource(if (state.submitting) R.string.inventory_saving else R.string.inventory_save_replenishment),
        onSaveReplenishment, !state.submitting, secondary = true)
    Spacer(Modifier.height(10.dp))
    state.stock.lastReplenishment?.let { LastReplenishmentCard(it, state.unit) }
    state.replenishmentErrorCode?.let { code ->
        Spacer(Modifier.height(12.dp))
        TataCard(containerColor = com.vitahealth.tata.shared.design.theme.TataErrorSurface) {
            InventoryText(stringResource(inventoryErrorMessageRes(code)), 11, TataError)
        }
    }
    if (state.justRegistered) {
        Spacer(Modifier.height(12.dp))
        TataCard(containerColor = TataMint) {
            InventoryText(stringResource(R.string.inventory_replenishment_saved), 11, AvailableGreen, FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StockCard(state: InventoryUiState.Ready) {
    val stock = state.stock
    val isLow = stock.status == StockStatus.LOW
    Surface(shape = RoundedCornerShape(18.dp), color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))) {
        Box(Modifier.fillMaxWidth().heightIn(min = 150.dp)
            .background(Brush.horizontalGradient(if (isLow) listOf(Color(0xFFFFF3E2), Color(0xFFFBE8C7))
                else listOf(Color(0xFFE8F5EB), Color(0xFFDDEDE1))))) {
            Column(Modifier.padding(start = 16.dp, top = 16.dp, bottom = 18.dp).widthIn(max = 190.dp)) {
                InventoryText(state.medicationName, 17, TataText, FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                InventoryText(if (state.unit.isBlank()) stringResource(R.string.inventory_units_remaining_no_unit, stock.remainingStock)
                    else stringResource(R.string.inventory_units_remaining, stock.remainingStock, state.unit), 18, TataText, FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                InventoryText(stock.daysRemaining?.let { stringResource(R.string.inventory_days_estimate, it) }
                    ?: stringResource(R.string.inventory_days_no_estimate), 12, TataMuted)
            }
            Column(Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                InventoryText(stringResource(if (isLow) R.string.inventory_status_low else R.string.inventory_status_available),
                    12, if (isLow) Color(0xFFD94759) else AvailableGreen, FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                // Native ring represents stock status; the day estimate always comes from the API.
                Box(Modifier.size(78.dp).border(9.dp, if (isLow) Color(0xFFE1AE4B) else AvailableGreen, CircleShape), contentAlignment = Alignment.Center) {
                    Column(Modifier.size(54.dp).background(Color.White, CircleShape), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        InventoryText(stock.daysRemaining?.toString() ?: "—", 20, TataDeepNavy, FontWeight.Bold)
                        InventoryText(stringResource(R.string.inventory_days_short), 9, TataMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun LastReplenishmentCard(batch: InventoryBatch, unit: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp))) {
        Box(Modifier.fillMaxWidth().heightIn(min = 92.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFFECF4FB), Color(0xFFDFECF6))))) {
            Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 64.dp, bottom = 12.dp)) {
                InventoryText(stringResource(R.string.inventory_last_replenishment), 12, TataText, FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                InventoryText(batchDateFormatter.format(batch.registeredAt), 15, TataText, FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                val quantity = if (unit.isBlank()) stringResource(R.string.inventory_batch_summary_no_unit, batch.quantity)
                    else stringResource(R.string.inventory_batch_summary, batch.quantity, unit)
                InventoryText(listOfNotNull(quantity, batch.lot?.takeIf { it.isNotBlank() }).joinToString(", "), 11, TataMuted)
            }
            Box(Modifier.align(Alignment.TopEnd).padding(top = 14.dp, end = 17.dp)
                .background(TataMint, CircleShape).padding(horizontal = 14.dp, vertical = 5.dp)) {
                InventoryText(stringResource(R.string.inventory_registered_badge), 10, AvailableGreen)
            }
            val context = androidx.compose.ui.platform.LocalContext.current
            coil3.compose.AsyncImage(model = coil3.request.ImageRequest.Builder(context)
                .data(R.raw.figma_inventory_medication).decoderFactory(coil3.svg.SvgDecoder.Factory()).build(), contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 27.dp, bottom = 18.dp).size(24.dp))
        }
    }
}

@Composable
private fun InventoryAction(text: String, onClick: () -> Unit, enabled: Boolean, secondary: Boolean = false) {
    Surface(onClick = onClick, enabled = enabled, shape = CircleShape,
        color = if (secondary) TataLavender else TataNavy,
        border = if (secondary) BorderStroke(1.dp, Color(0xFFC2B5E5)) else null,
        modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape)) {
        Box(contentAlignment = Alignment.Center) {
            InventoryText(text, 15, if (secondary) TataNavy else Color.White, FontWeight.SemiBold)
        }
    }
}

@Composable
private fun InventoryText(text: String, size: Int, color: Color, weight: FontWeight = FontWeight.Normal) {
    Text(text, style = TextStyle(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
        fontSize = size.sp, lineHeight = (size * 1.3f).sp, fontWeight = weight, color = color))
}

@Composable
private fun NotInitializedBody(
    state: InventoryUiState.NotInitialized,
    onInitialQuantityChange: (String) -> Unit,
    onThresholdChange: (String) -> Unit,
    onDefineInitial: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
    focusRequester: FocusRequester? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (isError && errorLabel != null) errorLabel else label,
            color = if (isError) TataError else TataMuted,
            fontSize = 11.sp, lineHeight = 14.sp,
            fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            textStyle = TextStyle(fontFamily = FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)), fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).tataPrototypeShadow(8.dp, RoundedCornerShape(15.dp))
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White,
                focusedBorderColor = if (isError) TataError else TataBorder,
                unfocusedBorderColor = if (isError) TataError else Color.Transparent,
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
    "MEDICATION_NOT_FOUND" -> R.string.inventory_error_medication_not_found
    "MEDICATION_INACTIVE" -> R.string.inventory_error_medication_inactive
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
