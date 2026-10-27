package com.vitahealth.tata.intake.presentation.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.intake.presentation.agenda.screens.IntakeAgendaScreen
import com.vitahealth.tata.intake.presentation.agenda.viewmodel.IntakeAgendaViewModel

@Composable
fun IntakeAgendaRoute(
    factory: IntakeAgendaViewModel.Factory,
    onOpenDose: (String) -> Unit,
    onHome: () -> Unit,
    onMedications: (() -> Unit)? = null,
    onNotes: (() -> Unit)? = null,
) {
    val model: IntakeAgendaViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    IntakeAgendaScreen(
        state,
        model::selectDay,
        model::moveWeek,
        model::refresh,
        onOpenDose,
        onHome,
        onMedications,
        onNotes,
    )
}
