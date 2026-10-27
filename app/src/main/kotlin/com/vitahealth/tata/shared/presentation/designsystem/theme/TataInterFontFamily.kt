package com.vitahealth.tata.shared.presentation.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.vitahealth.tata.R

/**
 * Inter 4.001 from the existing licensed family, using genuine font masters.
 *
 * The standard face retains Codex's original subset. Additional weights share the
 * same font version and optical size, avoiding synthetic weight interpolation.
 */
val TataInterFontFamily =
    FontFamily(
        Font(R.font.tata_inter, FontWeight.Normal),
        Font(R.font.tata_inter_medium, FontWeight.Medium),
        Font(R.font.tata_inter_semibold, FontWeight.SemiBold),
        Font(R.font.tata_inter_bold, FontWeight.Bold),
    )
