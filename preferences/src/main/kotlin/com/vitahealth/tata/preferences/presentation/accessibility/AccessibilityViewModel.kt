package com.vitahealth.tata.preferences.presentation.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.preferences.application.PreferenceUpdate
import com.vitahealth.tata.preferences.application.commands.SyncUserPreferencesCommand
import com.vitahealth.tata.preferences.application.commands.UpdateHighContrastCommand
import com.vitahealth.tata.preferences.application.commands.UpdateReducedMotionCommand
import com.vitahealth.tata.preferences.application.commands.UpdateTextSizeCommand
import com.vitahealth.tata.preferences.application.commands.UpdateVoiceConfirmationCommand
import com.vitahealth.tata.preferences.application.handlers.ObserveAccessibilityPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.SyncUserPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateHighContrastCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateReducedMotionCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateTextSizeCommandHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateVoiceConfirmationCommandHandler
import com.vitahealth.tata.preferences.domain.model.TextSizeLevel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccessibilityViewModel(
    private val userId: String,
    observeAccessibility: ObserveAccessibilityPreferencesQueryHandler,
    private val updateTextSize: UpdateTextSizeCommandHandler,
    private val updateHighContrast: UpdateHighContrastCommandHandler,
    private val updateReducedMotion: UpdateReducedMotionCommandHandler,
    private val updateVoiceConfirmation: UpdateVoiceConfirmationCommandHandler,
    private val syncPreferences: SyncUserPreferencesCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(AccessibilityUiState())
    val state: StateFlow<AccessibilityUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeAccessibility().collect { preferences ->
                _state.update { it.copy(preferences = preferences) }
            }
        }
        // The device copy is already shown; this only brings it in line with the backend.
        viewModelScope.launch { syncPreferences(SyncUserPreferencesCommand(userId)) }
    }

    fun onLargeTextChange(enabled: Boolean) {
        val textSize = if (enabled) TextSizeLevel.LARGE else TextSizeLevel.MEDIUM
        if (_state.value.preferences.textSize == textSize) return
        save(
            send = { updateTextSize(UpdateTextSizeCommand(userId, textSize)) },
            onSaved = if (enabled) AccessibilityMessage.LargeTextSaved else AccessibilityMessage.StandardTextSaved,
        )
    }

    fun onHighContrastChange(enabled: Boolean) {
        if (_state.value.preferences.highContrast == enabled) return
        save(
            send = { updateHighContrast(UpdateHighContrastCommand(userId, enabled)) },
            onSaved = if (enabled) AccessibilityMessage.HighContrastSaved else AccessibilityMessage.StandardContrastSaved,
        )
    }

    fun onReducedMotionChange(enabled: Boolean) {
        if (_state.value.preferences.reducedMotion == enabled) return
        save(
            send = { updateReducedMotion(UpdateReducedMotionCommand(userId, enabled)) },
            onSaved = if (enabled) AccessibilityMessage.ReducedMotionSaved else AccessibilityMessage.StandardMotionSaved,
        )
    }

    fun onVoiceConfirmationChange(enabled: Boolean) {
        if (_state.value.preferences.voiceConfirmation == enabled) return
        save(
            send = { updateVoiceConfirmation(UpdateVoiceConfirmationCommand(userId, enabled)) },
            onSaved = if (enabled) AccessibilityMessage.VoiceConfirmationSaved else AccessibilityMessage.VoiceConfirmationOffSaved,
        )
    }

    private fun save(
        send: suspend () -> AppResult<PreferenceUpdate>,
        onSaved: AccessibilityMessage,
    ) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            when (val result = send()) {
                is AppResult.Success -> _state.update {
                    val message = if (result.value.syncedWithServer) onSaved else AccessibilityMessage.SavedOffline
                    it.copy(isSaving = false, message = message)
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isSaving = false, message = errorMessage(result))
                }
            }
        }
    }

    private fun errorMessage(failure: AppResult.Failure): AccessibilityMessage =
        when (failure.code) {
            "INVALID_USER_REFERENCE" -> AccessibilityMessage.ErrorUser
            "REQUEST_VALIDATION_FAILED" -> AccessibilityMessage.ErrorRejected
            else -> AccessibilityMessage.ErrorGeneric
        }

    class Factory(
        private val userId: String,
        private val observeAccessibility: ObserveAccessibilityPreferencesQueryHandler,
        private val updateTextSize: UpdateTextSizeCommandHandler,
        private val updateHighContrast: UpdateHighContrastCommandHandler,
        private val updateReducedMotion: UpdateReducedMotionCommandHandler,
        private val updateVoiceConfirmation: UpdateVoiceConfirmationCommandHandler,
        private val syncPreferences: SyncUserPreferencesCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccessibilityViewModel(
                userId = userId,
                observeAccessibility = observeAccessibility,
                updateTextSize = updateTextSize,
                updateHighContrast = updateHighContrast,
                updateReducedMotion = updateReducedMotion,
                updateVoiceConfirmation = updateVoiceConfirmation,
                syncPreferences = syncPreferences,
            ) as T
    }
}
