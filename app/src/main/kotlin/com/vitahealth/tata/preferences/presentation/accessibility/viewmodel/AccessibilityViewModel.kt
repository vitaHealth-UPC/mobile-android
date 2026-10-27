package com.vitahealth.tata.preferences.presentation.accessibility.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.preferences.application.commands.ChangeApplicationLanguageCommand
import com.vitahealth.tata.preferences.application.commands.ChangeApplicationLanguageCommandHandler
import com.vitahealth.tata.preferences.application.commands.SyncUserPreferencesCommand
import com.vitahealth.tata.preferences.application.commands.SyncUserPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateDarkThemeCommand
import com.vitahealth.tata.preferences.application.commands.UpdateDarkThemeCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateHighContrastCommand
import com.vitahealth.tata.preferences.application.commands.UpdateHighContrastCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateReadingAssistanceCommand
import com.vitahealth.tata.preferences.application.commands.UpdateReadingAssistanceCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateReducedMotionCommand
import com.vitahealth.tata.preferences.application.commands.UpdateReducedMotionCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateTextSizeCommand
import com.vitahealth.tata.preferences.application.commands.UpdateTextSizeCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateVoiceConfirmationCommand
import com.vitahealth.tata.preferences.application.commands.UpdateVoiceConfirmationCommandHandler
import com.vitahealth.tata.preferences.application.queries.ObserveAccessibilityPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.readmodels.PreferenceUpdate
import com.vitahealth.tata.preferences.domain.model.valueobjects.AppLanguage
import com.vitahealth.tata.preferences.domain.model.valueobjects.TextSizeLevel
import com.vitahealth.tata.preferences.presentation.accessibility.mappers.accessibilityErrorMessage
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    private val updateDarkTheme: UpdateDarkThemeCommandHandler,
    private val updateReducedMotion: UpdateReducedMotionCommandHandler,
    private val updateVoiceConfirmation: UpdateVoiceConfirmationCommandHandler,
    private val updateReadingAssistance: UpdateReadingAssistanceCommandHandler,
    private val syncPreferences: SyncUserPreferencesCommandHandler,
    language: Flow<AppLanguage> = flowOf(AppLanguage.SPANISH),
    private val changeLanguage: ChangeApplicationLanguageCommandHandler? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(AccessibilityUiState())
    val state: StateFlow<AccessibilityUiState> = _state.asStateFlow()
    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            language.collect { selected ->
                _state.update { it.copy(appLanguage = selected, isLanguageSaving = false) }
            }
        }
        viewModelScope.launch {
            observeAccessibility().collect { preferences ->
                _state.update { it.copy(preferences = preferences) }
            }
        }
        // The device copy is already shown; this only brings it in line with the backend.
        refresh()
    }

    /** Reconciles pending device changes when this screen returns to the foreground. */
    fun refresh() {
        if (_state.value.isSaving || syncJob?.isActive == true) return
        syncJob =
            viewModelScope.launch {
                val result = syncPreferences(SyncUserPreferencesCommand(userId))
                currentCoroutineContext().ensureActive()
                if (result is AppResult.Success) {
                    _state.update {
                        if (it.message == AccessibilityMessage.SavedOffline) it.copy(message = null)
                        else it
                    }
                }
            }
    }

    /** Updates a local, owner-scoped setting and lets the activity reload its resources. */
    fun onLanguageChange() {
        val handler = changeLanguage ?: return
        val current = _state.value
        if (current.isLanguageSaving || current.isSaving) return
        val next = current.appLanguage.next()
        _state.update { it.copy(isLanguageSaving = true, languageError = null) }
        viewModelScope.launch {
            val result = handler(ChangeApplicationLanguageCommand(userId, next))
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(isLanguageSaving = false, appLanguage = result.value)
                    }
                is AppResult.Failure ->
                    _state.update {
                        it.copy(
                            isLanguageSaving = false,
                            languageError = if (
                                result.code in setOf(
                                    "AUTHENTICATION_REQUIRED", "RESOURCE_ACCESS_DENIED",
                                    "SESSION_CHANGED"
                                )
                            ) "La sesión no permite cambiar el idioma."
                            else "No se pudo guardar el idioma.",
                        )
                    }
            }
        }
    }

    fun onLargeTextChange(enabled: Boolean) {
        val textSize = if (enabled) TextSizeLevel.LARGE else TextSizeLevel.MEDIUM
        if (_state.value.preferences.textSize == textSize) return
        save(
            send = { updateTextSize(UpdateTextSizeCommand(userId, textSize)) },
            onSaved =
                if (enabled) AccessibilityMessage.LargeTextSaved
                else AccessibilityMessage.StandardTextSaved,
        )
    }

    fun onHighContrastChange(enabled: Boolean) {
        if (_state.value.preferences.highContrast == enabled) return
        save(
            send = { updateHighContrast(UpdateHighContrastCommand(userId, enabled)) },
            onSaved =
                if (enabled) AccessibilityMessage.HighContrastSaved
                else AccessibilityMessage.StandardContrastSaved,
        )
    }

    fun onDarkThemeChange(enabled: Boolean) {
        if (_state.value.preferences.darkTheme == enabled) return
        save(
            send = { updateDarkTheme(UpdateDarkThemeCommand(userId, enabled)) },
            onSaved =
                if (enabled) AccessibilityMessage.DarkThemeSaved
                else AccessibilityMessage.LightThemeSaved,
        )
    }

    fun onReducedMotionChange(enabled: Boolean) {
        if (_state.value.preferences.reducedMotion == enabled) return
        save(
            send = { updateReducedMotion(UpdateReducedMotionCommand(userId, enabled)) },
            onSaved =
                if (enabled) AccessibilityMessage.ReducedMotionSaved
                else AccessibilityMessage.StandardMotionSaved,
        )
    }

    fun onVoiceConfirmationChange(enabled: Boolean) {
        if (_state.value.preferences.voiceConfirmation == enabled) return
        save(
            send = { updateVoiceConfirmation(UpdateVoiceConfirmationCommand(userId, enabled)) },
            onSaved =
                if (enabled) AccessibilityMessage.VoiceConfirmationSaved
                else AccessibilityMessage.VoiceConfirmationOffSaved,
        )
    }

    fun onReadingAssistanceChange(enabled: Boolean) {
        if (_state.value.preferences.readingAssistance == enabled) return
        save(
            send = { updateReadingAssistance(UpdateReadingAssistanceCommand(userId, enabled)) },
            onSaved =
                if (enabled) AccessibilityMessage.ReadingAssistanceSaved
                else AccessibilityMessage.StandardReadingSaved,
        )
    }

    private fun save(
        send: suspend () -> AppResult<PreferenceUpdate>,
        onSaved: AccessibilityMessage,
    ) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val result = send()
            currentCoroutineContext().ensureActive()
            when (result) {
                is AppResult.Success ->
                    _state.update {
                        val message =
                            if (result.value.syncedWithServer) onSaved
                            else AccessibilityMessage.SavedOffline
                        it.copy(isSaving = false, message = message)
                    }

                is AppResult.Failure ->
                    _state.update {
                        it.copy(isSaving = false, message = accessibilityErrorMessage(result))
                    }
            }
        }
    }

    class Factory(
        private val userId: String,
        private val observeAccessibility: ObserveAccessibilityPreferencesQueryHandler,
        private val updateTextSize: UpdateTextSizeCommandHandler,
        private val updateHighContrast: UpdateHighContrastCommandHandler,
        private val updateDarkTheme: UpdateDarkThemeCommandHandler,
        private val updateReducedMotion: UpdateReducedMotionCommandHandler,
        private val updateVoiceConfirmation: UpdateVoiceConfirmationCommandHandler,
        private val updateReadingAssistance: UpdateReadingAssistanceCommandHandler,
        private val syncPreferences: SyncUserPreferencesCommandHandler,
        private val language: Flow<AppLanguage> = flowOf(AppLanguage.SPANISH),
        private val changeLanguage: ChangeApplicationLanguageCommandHandler? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AccessibilityViewModel(
                userId = userId,
                observeAccessibility = observeAccessibility,
                updateTextSize = updateTextSize,
                updateHighContrast = updateHighContrast,
                updateDarkTheme = updateDarkTheme,
                updateReducedMotion = updateReducedMotion,
                updateVoiceConfirmation = updateVoiceConfirmation,
                updateReadingAssistance = updateReadingAssistance,
                syncPreferences = syncPreferences,
                language = language,
                changeLanguage = changeLanguage,
            )
                as T
    }
}
