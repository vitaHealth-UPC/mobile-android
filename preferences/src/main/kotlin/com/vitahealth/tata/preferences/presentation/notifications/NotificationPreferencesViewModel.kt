package com.vitahealth.tata.preferences.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommand
import com.vitahealth.tata.preferences.application.handlers.GetNotificationPreferencesQueryHandler
import com.vitahealth.tata.preferences.application.handlers.UpdateNotificationPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQuery
import com.vitahealth.tata.preferences.domain.model.ChannelType
import com.vitahealth.tata.preferences.domain.model.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.QuietHours
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationPreferencesViewModel(
    private val userId: String,
    private val getPreferences: GetNotificationPreferencesQueryHandler,
    private val updatePreferences: UpdateNotificationPreferencesCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationPreferencesUiState())
    val state: StateFlow<NotificationPreferencesUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            when (val result = getPreferences(GetNotificationPreferencesQuery(userId))) {
                is AppResult.Success -> _state.update {
                    it.copy(isLoading = false, preferences = result.value)
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, message = errorMessage(result))
                }
            }
        }
    }

    fun onQuietHoursEnabledChange(enabled: Boolean) {
        val current = _state.value.preferences ?: return
        if ((current.quietHours != null) == enabled) return
        save(current.copy(quietHours = if (enabled) QuietHours.Default else null))
    }

    fun onQuietHoursStartChange(hour: Int, minute: Int) {
        val current = _state.value.preferences ?: return
        val hours = current.quietHours ?: return
        changeQuietHours(current, hours.endHour, hours.endMinute, hour, minute)
    }

    fun onQuietHoursEndChange(hour: Int, minute: Int) {
        val current = _state.value.preferences ?: return
        val hours = current.quietHours ?: return
        changeQuietHours(current, hour, minute, hours.startHour, hours.startMinute)
    }

    fun onChannelChange(type: ChannelType, enabled: Boolean) {
        val current = _state.value.preferences ?: return
        if (current.channels.firstOrNull { it.type == type }?.enabled == enabled) return
        val channels = if (current.channels.any { it.type == type }) {
            current.channels.map { if (it.type == type) it.copy(enabled = enabled) else it }
        } else {
            current.channels + NotificationChannel(type, enabled)
        }
        save(current.copy(channels = channels))
    }

    private fun changeQuietHours(
        current: NotificationPreferences,
        endHour: Int,
        endMinute: Int,
        startHour: Int,
        startMinute: Int,
    ) {
        val hours = try {
            QuietHours(startHour, startMinute, endHour, endMinute)
        } catch (exception: IllegalArgumentException) {
            _state.update { it.copy(message = NotificationMessage.ErrorInvalidHours) }
            return
        }
        save(current.copy(quietHours = hours))
    }

    private fun save(updated: NotificationPreferences) {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val command = UpdateNotificationPreferencesCommand(userId, updated.quietHours, updated.channels)
            when (val result = updatePreferences(command)) {
                is AppResult.Success -> _state.update {
                    it.copy(isSaving = false, preferences = result.value, message = NotificationMessage.Saved)
                }

                is AppResult.Failure -> _state.update {
                    it.copy(isSaving = false, message = errorMessage(result))
                }
            }
        }
    }

    private fun errorMessage(failure: AppResult.Failure): NotificationMessage =
        when (failure.code) {
            "INVALID_USER_REFERENCE" -> NotificationMessage.ErrorUser
            "REQUEST_VALIDATION_FAILED", "DUPLICATE_CHANNEL" -> NotificationMessage.ErrorRejected
            "NETWORK_UNAVAILABLE" -> NotificationMessage.ErrorOffline
            else -> NotificationMessage.ErrorGeneric
        }

    class Factory(
        private val userId: String,
        private val getPreferences: GetNotificationPreferencesQueryHandler,
        private val updatePreferences: UpdateNotificationPreferencesCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationPreferencesViewModel(userId, getPreferences, updatePreferences) as T
    }
}
