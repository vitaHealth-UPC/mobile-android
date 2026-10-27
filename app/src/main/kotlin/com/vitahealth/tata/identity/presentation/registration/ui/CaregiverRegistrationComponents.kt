package com.vitahealth.tata.identity.presentation.registration.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataCream
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataDeepNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataLavender
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataNavy
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataPrototypeShadow
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
@Composable
internal fun RegistrationProgress(verificationActive: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(
                Brush.horizontalGradient(listOf(TataLavender, Color(0xFFECF4FB))),
                RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RegistrationText("1  Cuenta", 11, TataDeepNavy, FontWeight.SemiBold)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.width(72.dp).height(4.dp).background(TataDeepNavy, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(22.dp))
        RegistrationText(
            "2  Verificación",
            11,
            if (verificationActive) TataDeepNavy else tataMutedColor(),
            if (verificationActive) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
internal fun VerificationCard(
    code: String,
    onCodeChange: (String) -> Unit,
    enabled: Boolean,
    codeSent: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = TataCream,
        modifier = Modifier.fillMaxWidth().tataPrototypeShadow(8.dp, RoundedCornerShape(18.dp)),
    ) {
        Column(Modifier.heightIn(min = 126.dp).padding(horizontal = 16.dp, vertical = 14.dp)) {
            RegistrationText("Verifica tu correo", 16, tataTextColor(), FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            RegistrationText(
                if (codeSent)
                    "Te enviamos un código de 6 dígitos. Escríbelo para habilitar tu cuenta."
                else "Crea tu cuenta para recibir un código de 6 dígitos y habilitar tu acceso.",
                12,
                tataMutedColor(),
            )
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = code,
                onValueChange = { value -> onCodeChange(value.filter(Char::isDigit).take(6)) },
                enabled = enabled,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle =
                    TextStyle(
                        fontFamily = FontFamily(Font(R.font.tata_inter)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TataNavy,
                        letterSpacing = 6.sp,
                    ),
                modifier =
                    Modifier.fillMaxWidth().heightIn(min = 32.dp).semantics {
                        contentDescription = "Código de verificación"
                    },
            )
        }
    }
}

@Composable
internal fun RegistrationButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    secondary: Boolean = false,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (secondary) TataLavender else TataNavy,
        contentColor = if (secondary) TataNavy else Color.White,
        border = if (secondary) BorderStroke(1.dp, Color(0xFFC2B5E5)) else null,
        modifier = Modifier.fillMaxWidth().height(56.dp).tataPrototypeShadow(8.dp, CircleShape),
    ) {
        Box(contentAlignment = Alignment.Center) {
            RegistrationText(
                text,
                15,
                if (secondary) TataNavy else Color.White,
                FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun RegistrationMessage(title: String, body: String, background: Color, tint: Color) {
    Column(
        Modifier.fillMaxWidth()
            .background(background, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        RegistrationText(title, 11, tint, FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            style =
                TextStyle(
                    fontFamily = FontFamily(Font(R.font.tata_inter)),
                    fontSize = 8.5.sp,
                    lineHeight = 12.sp,
                    color = tataMutedColor(),
                ),
        )
    }
}

@Composable
internal fun RegistrationText(
    text: String,
    size: Int,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier,
        style =
            TextStyle(
                fontFamily = FontFamily(Font(R.font.tata_inter)),
                fontSize = size.sp,
                lineHeight = (size * 1.3f).sp,
                fontWeight = weight,
            ),
        color = color,
    )
}
