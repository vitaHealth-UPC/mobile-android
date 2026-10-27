package com.vitahealth.tata.identity.presentation.access.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vitahealth.tata.identity.presentation.access.mappers.sessionAccessMessageRes
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataError
@Composable
internal fun AccessError(code: String) {
    Text(
        stringResource(sessionAccessMessageRes(code)),
        color = TataError,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
}
