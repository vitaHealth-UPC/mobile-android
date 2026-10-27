package com.vitahealth.tata.carelink.presentation.profiles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.carelink.application.readmodels.LinkedAdult
import com.vitahealth.tata.carelink.application.readmodels.ProfileLinkingCode
import com.vitahealth.tata.carelink.presentation.profiles.screens.CaregiverProfilesScreen
import com.vitahealth.tata.carelink.presentation.profiles.viewmodel.CaregiverProfilesViewModel

@Composable
fun CaregiverProfilesRoute(
    factory: CaregiverProfilesViewModel.Factory,
    onOpenAdult: (LinkedAdult) -> Unit,
    onAdultConsent: (ProfileLinkingCode) -> Unit,
    onSignOut: () -> Unit,
    onLinkSocial: (() -> Unit)? = null,
) {
    val vm: CaregiverProfilesViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, vm) {
        val observer =
            androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) vm.onResume()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    CaregiverProfilesScreen(
        state,
        vm::form,
        vm::name,
        vm::date,
        vm::contact,
        vm::relationship,
        vm::phone,
        vm::save,
        vm::refresh,
        onOpenAdult,
        onAdultConsent,
        onSignOut,
        onLinkSocial,
    )
}
