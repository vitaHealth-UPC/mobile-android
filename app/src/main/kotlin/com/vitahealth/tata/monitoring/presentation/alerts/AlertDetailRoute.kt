package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.presentation.alerts.screens.AlertDetailScreen
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertDetailViewModel
import com.vitahealth.tata.monitoring.presentation.notes.ui.NoteComposerDialog
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

@Composable
fun AlertDetailRoute(
    factory: AlertDetailViewModel.Factory,
    onBack: () -> Unit,
    onTabSelected: (CaregiverTab) -> Unit,
    onOpenProfile: (String) -> Unit = {},
) {
    val model: AlertDetailViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val composer by model.noteComposer.state.collectAsState()
    val contact by model.contact.collectAsState()
    val person by model.person.collectAsState()
    val history by model.history.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.onResume()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    AlertDetailScreen(
        state,
        onBack,
        model::load,
        onTabSelected,
        model::updateStatus,
        composer.saved,
        model.noteComposer::open,
        contact,
        model::loadContact,
        person = person,
        history = history,
        onOpenProfile = { person?.let { onOpenProfile(it.fullName) } },
    )
    NoteComposerDialog(
        composer,
        onSave = { model.noteComposer.save(it) },
        onDismiss = model.noteComposer::dismiss,
    )
}
