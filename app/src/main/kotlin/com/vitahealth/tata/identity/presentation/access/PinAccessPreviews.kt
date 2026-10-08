package com.vitahealth.tata.identity.presentation.access

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vitahealth.tata.shared.design.theme.TataTheme

@Preview(name = "Figma 06 - PIN access", widthDp = 393, heightDp = 852)
@Composable
private fun PinAccessPreview() = PinDesignPreview()

@Preview(name = "Figma 29 - PIN setup", widthDp = 393, heightDp = 852)
@Composable
private fun PinSetupPreview() = PinDesignPreview(setup = true)

@Preview(name = "Figma 30 - Incorrect PIN", widthDp = 393, heightDp = 852)
@Composable
private fun PinIncorrectPreview() = PinDesignPreview(error = "INVALID_PIN")

@Preview(name = "Figma 31 - Temporarily blocked", widthDp = 393, heightDp = 852)
@Composable
private fun PinBlockedPreview() = PinDesignPreview(error = "PIN_LOCKED")

@Composable
private fun PinDesignPreview(setup: Boolean = false, error: String? = null) {
    TataTheme {
        PinAccessScreen(SessionAccessUiState(pin = "1111", error = error), "Rosa Vargas", setup, {}, {}, {})
    }
}
