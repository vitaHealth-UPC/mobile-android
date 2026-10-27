package com.vitahealth.tata.inventory.presentation.inventory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.accessibility.LocalTataAccessibility
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow

@Composable
internal fun InventoryAction(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    secondary: Boolean = false,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (secondary) tataTonalColor(TataLavender) else tataAccentColor(TataNavy),
        border =
            if (secondary) BorderStroke(1.dp, tataOutlineColor(Color(0xFFC2B5E5)))
            else null,
        modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape),
    ) {
        Box(contentAlignment = Alignment.Center) {
            InventoryText(
                text,
                15,
                if (secondary) tataAccentColor(TataNavy)
                else MaterialTheme.colorScheme.onPrimary,
                FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun InventoryText(
    text: String,
    size: Int,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
) {
    Text(
        text,
        style =
            TextStyle(
                fontFamily = FontFamily(Font(R.font.tata_inter)),
                fontSize = size.sp,
                lineHeight = (size * 1.3f).sp,
                fontWeight = weight,
                color = color,
            ),
    )
}

/** Keeps Figma's light error red and Material's legible dark error red. */
@Composable
internal fun inventoryErrorColor(): Color =
    if (LocalTataAccessibility.current.darkTheme) MaterialTheme.colorScheme.error
    else TataError

/**
 * Numeric field that turns red and swaps its label to [errorLabel] when [isError] (e.g. the
 * "Cantidad inválida" state in frame 294-3255). A local variant because the shared
 * [com.vitahealth.tata.shared.presentation.designsystem.components.TataFormField] has no error state and lives in
 * another module.
 */
@Composable
internal fun InventoryQuantityField(
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
            text = if (isError && !errorLabel.isNullOrBlank()) errorLabel else label,
            color = if (isError) inventoryErrorColor() else tataMutedColor(),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontFamily = FontFamily(Font(R.font.tata_inter)),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = isError,
            placeholder = {
                if (isError && errorLabel != null) {
                    Text(errorLabel, color = inventoryErrorColor())
                }
            },
            supportingText =
                if (isError)
                    ({
                        Text(
                            stringResource(R.string.inventory_error_invalid_quantity),
                            color = inventoryErrorColor(),
                            fontFamily = FontFamily(Font(R.font.tata_inter)),
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                        )
                    })
                else null,
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            textStyle =
                TextStyle(fontFamily = FontFamily(Font(R.font.tata_inter)), fontSize = 14.sp),
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .tataPrototypeShadow(8.dp, RoundedCornerShape(15.dp))
                    .then(
                        if (focusRequester != null) Modifier.focusRequester(focusRequester)
                        else Modifier
                    ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = tataCardColor(),
                    unfocusedContainerColor = tataCardColor(),
                    disabledContainerColor = tataCardColor(),
                    errorContainerColor = tataCardColor(),
                    focusedBorderColor = if (isError) inventoryErrorColor() else tataOutlineColor(TataBorder),
                    unfocusedBorderColor = if (isError) inventoryErrorColor() else Color.Transparent,
                    errorBorderColor = inventoryErrorColor(),
                    focusedTextColor = tataTextColor(),
                    unfocusedTextColor = tataTextColor(),
                ),
        )
    }
}
