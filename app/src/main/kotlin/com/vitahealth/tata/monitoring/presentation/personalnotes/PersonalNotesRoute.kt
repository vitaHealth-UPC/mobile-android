package com.vitahealth.tata.monitoring.presentation.personalnotes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.presentation.personalnotes.screens.PersonalNotesScreen
import com.vitahealth.tata.monitoring.presentation.personalnotes.ui.PersonalNoteComposer
import com.vitahealth.tata.monitoring.presentation.personalnotes.viewmodel.PersonalNotesViewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.AdultTab

@Composable
fun PersonalNotesRoute(
    factory: PersonalNotesViewModel.Factory,
    onTab: (AdultTab) -> Unit,
    onSignIn: () -> Unit,
) {
    val model: PersonalNotesViewModel = viewModel(factory = factory)
    val state by model.state.collectAsStateWithLifecycle()
    PersonalNotesScreen(
        state,
        model::refresh,
        model::filter,
        { model.compose(true) },
        onTab,
        onSignIn,
    )
    if (state.composing)
        PersonalNoteComposer(state.saving, state.saveError, model::save, { model.compose(false) })
}
