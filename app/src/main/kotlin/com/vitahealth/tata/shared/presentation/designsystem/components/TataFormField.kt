package com.vitahealth.tata.shared.presentation.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataBorder
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataMuted
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataText
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataCardColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataOutlineColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor

@Composable
fun TataFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    softSurface: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = tataMutedColor(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontFamily =
                        if (softSurface) FontFamily(Font(R.font.tata_inter))
                        else MaterialTheme.typography.labelSmall.fontFamily,
                    lineHeight =
                        if (softSurface) 14.sp else MaterialTheme.typography.labelSmall.lineHeight,
                    letterSpacing =
                        if (softSurface) 0.sp else MaterialTheme.typography.labelSmall.letterSpacing,
                ),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            textStyle =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontFamily =
                        if (softSurface) FontFamily(Font(R.font.tata_inter))
                        else MaterialTheme.typography.bodyMedium.fontFamily,
                ),
            onValueChange = onValueChange,
            enabled = enabled,
            modifier =
                Modifier.fillMaxWidth()
                    .then(
                        if (softSurface)
                            Modifier.heightIn(min = 58.dp)
                                .tataPrototypeShadow(8.dp, RoundedCornerShape(15.dp))
                        else Modifier
                    ),
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(15.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = tataCardColor(),
                    unfocusedContainerColor = tataCardColor(),
                    disabledContainerColor = tataCardColor(),
                    focusedBorderColor = tataOutlineColor(),
                    unfocusedBorderColor = if (softSurface) Color.Transparent else tataOutlineColor(),
                    disabledBorderColor = if (softSurface) Color.Transparent else tataOutlineColor(),
                    focusedTextColor = tataTextColor(),
                    unfocusedTextColor = tataTextColor(),
                    disabledTextColor = tataMutedColor(),
                ),
        )
    }
}
