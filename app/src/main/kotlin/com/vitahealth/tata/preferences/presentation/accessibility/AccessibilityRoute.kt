package com.vitahealth.tata.preferences.presentation.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.preferences.presentation.accessibility.screens.AccessibilityScreen
import com.vitahealth.tata.preferences.presentation.accessibility.viewmodel.AccessibilityViewModel

@Composable
fun AccessibilityRoute(
    factory: AccessibilityViewModel.Factory,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationBar: @Composable () -> Unit = {},
) {
    val viewModel: AccessibilityViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    AccessibilityScreen(
        state = state,
        onBack = onBack,
        onLargeTextChange = viewModel::onLargeTextChange,
        onDarkThemeChange = viewModel::onDarkThemeChange,
        onHighContrastChange = viewModel::onHighContrastChange,
        onReducedMotionChange = viewModel::onReducedMotionChange,
        onVoiceConfirmationChange = viewModel::onVoiceConfirmationChange,
        onReadingAssistanceChange = viewModel::onReadingAssistanceChange,
        onLanguageChange = viewModel::onLanguageChange,
        modifier = modifier,
        navigationBar = navigationBar,
    )
}
