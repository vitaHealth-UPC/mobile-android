package com.vitahealth.tata.monitoring.presentation.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.presentation.notes.screens.NotesScreen
import com.vitahealth.tata.monitoring.presentation.notes.ui.NoteComposerDialog
import com.vitahealth.tata.monitoring.presentation.notes.viewmodel.NotesViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

@Composable
fun NotesRoute(factory: NotesViewModel.Factory, onTabSelected: (CaregiverTab) -> Unit) {
    val model: NotesViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val person by model.person.collectAsState()
    val composer by model.composer.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    NotesScreen(
        state = state,
        caregiverId = model.caregiverId,
        noteSaved = composer.saved,
        onRetry = model::refresh,
        onAddNote = model.composer::open,
        onTabSelected = onTabSelected,
        person = person,
    )
    NoteComposerDialog(composer, onSave = model::saveNote, onDismiss = model.composer::dismiss)
}
