package com.vitahealth.tata.treatment.presentation.treatment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.shared.presentation.designsystem.components.TataButton
import com.vitahealth.tata.shared.presentation.designsystem.components.TataSvgIcon
import com.vitahealth.tata.shared.presentation.designsystem.theme.*
import com.vitahealth.tata.treatment.presentation.treatment.mappers.firstNameDetail

/** Shows no clinical data when access is denied and offers an explicit safe return destination. */
@Composable
internal fun TreatmentRestrictedContent(olderAdultName: String, onReturnToProfiles: () -> Unit) {
    Text(
        "Tratamiento de " + firstNameDetail(olderAdultName),
        fontFamily = FontFamily(Font(R.font.tata_serif)),
        fontSize = 29.sp,
        lineHeight = 35.sp,
        color = tataTextColor(),
    )
    Text(
        "Gestiona la pauta sin perder historial.",
        fontSize = 12.sp,
        color = tataMutedColor(),
        modifier = Modifier.padding(top = 4.dp),
    )
    Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
        TataSvgIcon(R.raw.treatment_restricted_state, Modifier.size(64.dp))
        Text("!", fontSize = 30.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFAD2E3D))
    }
    Column(
        Modifier.fillMaxWidth()
            .padding(top = 12.dp)
            .background(tataTonalColor(TataErrorSurface), RoundedCornerShape(20.dp))
            .heightIn(min = 226.dp)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Acceso restringido",
            fontSize = 21.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.SemiBold,
            color = tataTextColor(),
            textAlign = TextAlign.Center,
        )
        Text(
            "Este tratamiento no pertenece a una persona vinculada a tu cuenta.",
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = tataMutedColor(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
    TataButton(
        "Volver a Persona vinculada",
        onReturnToProfiles,
        modifier = Modifier.fillMaxWidth().padding(top = 52.dp),
    )
}
