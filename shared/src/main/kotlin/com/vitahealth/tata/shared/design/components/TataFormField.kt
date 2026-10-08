package com.vitahealth.tata.shared.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import com.vitahealth.tata.shared.design.theme.tataPrototypeShadow
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataMuted
import com.vitahealth.tata.shared.design.theme.TataText

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
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TataMuted,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp,
                fontFamily = if (softSurface) FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)) else MaterialTheme.typography.labelSmall.fontFamily,
                lineHeight = if (softSurface) 14.sp else MaterialTheme.typography.labelSmall.lineHeight,
                letterSpacing = if (softSurface) 0.sp else MaterialTheme.typography.labelSmall.letterSpacing),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = value,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp,
                fontFamily = if (softSurface) FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_inter)) else MaterialTheme.typography.bodyMedium.fontFamily),
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().then(if (softSurface)
                Modifier.heightIn(min = 58.dp).tataPrototypeShadow(8.dp, RoundedCornerShape(15.dp)) else Modifier),
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            shape = RoundedCornerShape(15.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedBorderColor = TataBorder,
                unfocusedBorderColor = if (softSurface) Color.Transparent else TataBorder,
                disabledBorderColor = if (softSurface) Color.Transparent else TataBorder,
                focusedTextColor = TataText,
                unfocusedTextColor = TataText,
                disabledTextColor = TataMuted,
            ),
        )
    }
}
