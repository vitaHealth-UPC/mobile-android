package com.vitahealth.tata.treatment.presentation.medication

import com.vitahealth.tata.treatment.presentation.medication.screens.MyMedicationsScreen
import com.vitahealth.tata.treatment.presentation.medication.viewmodel.MyMedicationsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/** Connects lifecycle refresh and navigation callbacks to the adult medication catalog. */
@Composable
fun MyMedicationsRoute(
    factory: MyMedicationsViewModel.Factory,
    onHome: () -> Unit,
    onAgenda: () -> Unit,
    onOpenDose: (String) -> Unit,
    onSignIn: () -> Unit,
    onNotes: (() -> Unit)? = null,
) {
    val model: MyMedicationsViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    MyMedicationsScreen(
        state,
        model::refresh,
        model::selectHistory,
        onHome,
        onAgenda,
        onOpenDose,
        onSignIn = onSignIn,
        onNotes = onNotes,
    )
}
