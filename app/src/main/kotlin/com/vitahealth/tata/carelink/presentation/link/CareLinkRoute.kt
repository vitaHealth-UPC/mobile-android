package com.vitahealth.tata.carelink.presentation.link

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.carelink.presentation.link.screens.CareLinkScreen
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkStep
import com.vitahealth.tata.carelink.presentation.link.viewmodel.CareLinkViewModel

@Composable
fun CareLinkRoute(
    factory: CareLinkViewModel.Factory,
    modifier: Modifier = Modifier,
    onConfirmed: (olderAdultId: String, olderAdultName: String) -> Unit = { _, _ -> },
    initialCode: String = "",
    initialOlderAdultId: String = "",
) {
    val vm: CareLinkViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    LaunchedEffect(initialCode) { if (initialCode.isNotBlank()) vm.onCodeChange(initialCode) }
    LaunchedEffect(initialOlderAdultId) {
        if (initialOlderAdultId.isNotBlank()) vm.previewOlderAdult(initialOlderAdultId)
    }
    LaunchedEffect(state.step, state.acceptedLink?.id, state.olderAdult?.id) {
        val link = state.acceptedLink
        val adult = state.olderAdult
        if (state.step == CareLinkStep.Confirmed && link != null && adult != null)
            onConfirmed(link.olderAdultId, adult.fullName)
    }
    CareLinkScreen(
        state,
        vm::onCodeChange,
        vm::sendLinkRequest,
        vm::acceptConsent,
        vm::rejectConsent,
        modifier,
    )
}
