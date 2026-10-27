package com.vitahealth.tata.identity.presentation.access

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.identity.presentation.access.screens.PinAccessScreen
import com.vitahealth.tata.identity.presentation.access.viewmodel.SessionAccessViewModel
@Composable
fun PinAccessRoute(
    factory: SessionAccessViewModel.Factory,
    olderAdultId: String,
    olderAdultName: String,
    setup: Boolean,
    onComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val vm: SessionAccessViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(state.subject, state.pinSaved) {
        if (state.subject != null || state.pinSaved) onComplete()
    }
    PinAccessScreen(
        state,
        olderAdultName,
        setup,
        vm::digit,
        { vm.pin(olderAdultId, setup) },
        onBack,
    )
}
