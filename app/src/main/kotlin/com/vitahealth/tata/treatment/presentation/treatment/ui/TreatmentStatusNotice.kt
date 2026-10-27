package com.vitahealth.tata.treatment.presentation.treatment.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.shared.presentation.designsystem.components.TataCard
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataAccentColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTonalColor

/** Outcome feedback outside the scroll area remains visible above caregiver navigation. */
@Composable
internal fun TreatmentStatusNotice(
    saved: Boolean,
    modifier: Modifier = Modifier,
    medicationInactive: Boolean = false,
) {
    TataCard(
        modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        containerColor = tataTonalColor(if (saved) Color(0xFFE8F5EB) else Color(0xFFFFF5E0)),
    ) {
        Text(
            if (medicationInactive) "Medicamento desactivado"
            else if (saved) "Cambios guardados" else "Tratamiento pausado",
            color = tataAccentColor(if (saved) Color(0xFF2E6E45) else Color(0xFF855E1F)),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (medicationInactive) "No se generarán nuevas tomas. El historial se conserva."
            else if (saved) "La nueva pauta se aplicará solo a tomas futuras."
            else "No se generarán nuevas tomas mientras permanezca pausado.",
            color = tataMutedColor(),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 5.dp),
        )
    }
}
