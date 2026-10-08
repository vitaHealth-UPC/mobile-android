package com.vitahealth.tata.shared.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.shared.design.theme.TataBorder
import com.vitahealth.tata.shared.design.theme.TataLavender
import com.vitahealth.tata.shared.design.theme.TataNavy

enum class TataButtonStyle { Primary, Secondary }

@Composable
fun TataButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: TataButtonStyle = TataButtonStyle.Primary,
) {
    val colors = when (style) {
        TataButtonStyle.Primary -> ButtonDefaults.buttonColors(
            containerColor = TataNavy,
            contentColor = Color.White,
        )
        TataButtonStyle.Secondary -> ButtonDefaults.buttonColors(
            containerColor = TataLavender,
            contentColor = TataNavy,
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = colors,
        border = if (style == TataButtonStyle.Secondary) BorderStroke(1.dp, TataBorder) else null,
    ) {
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}
