package com.vitahealth.tata.preferences.presentation.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPolicyCommand
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPolicyCommandHandler
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommand
import com.vitahealth.tata.preferences.application.commands.UpdateNotificationPreferencesCommandHandler
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQuery
import com.vitahealth.tata.preferences.application.queries.GetNotificationPreferencesQueryHandler
import com.vitahealth.tata.preferences.domain.model.valueobjects.ChannelType
import com.vitahealth.tata.preferences.domain.model.valueobjects.EmergencyContact
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationChannel
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPolicy
import com.vitahealth.tata.preferences.domain.model.valueobjects.NotificationPreferences
import com.vitahealth.tata.preferences.domain.model.valueobjects.QuietHours
import com.vitahealth.tata.preferences.presentation.notifications.mappers.notificationErrorMessage
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Coordinates notification reads and serialized form mutations without stale load publication. */
class NotificationPreferencesViewModel(
    private val userId: String,
    private val getPreferences: GetNotificationPreferencesQueryHandler,
    private val updatePreferences: UpdateNotificationPreferencesCommandHandler,
    private val updatePolicy: UpdateNotificationPolicyCommandHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationPreferencesUiState())
    val state: StateFlow<NotificationPreferencesUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        if (_state.value.isSaving) return
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, message = null) }
        loadJob =
            viewModelScope.launch {
                when (
                    val result =
                        getPreferences(GetNotificationPreferencesQuery(userId)).also {
                            currentCoroutineContext().ensureActive()
                        }
                ) {
                    is AppResult.Success ->
                        _state.update { it.copy(isLoading = false, preferences = result.value) }

                    is AppResult.Failure ->
                        _state.update { it.copy(isLoading = false, message = notificationErrorMessage(result)) }
                }
            }
    }

    fun onQuietHoursEnabledChange(enabled: Boolean) {
        if (_state.value.isSaving || _state.value.isLoading) return
        val current = _state.value.preferences ?: return
        if ((current.quietHours != null) == enabled) return
        save(current.copy(quietHours = if (enabled) QuietHours.Default else null))
    }

    fun onQuietHoursStartChange(hour: Int, minute: Int) {
        if (_state.value.isSaving || _state.value.isLoading) return
        val current = _state.value.preferences ?: return
        val hours = current.quietHours ?: return
        changeQuietHours(current, hours.endHour, hours.endMinute, hour, minute)
    }

    fun onQuietHoursEndChange(hour: Int, minute: Int) {
        if (_state.value.isSaving || _state.value.isLoading) return
        val current = _state.value.preferences ?: return
        val hours = current.quietHours ?: return
        changeQuietHours(current, hour, minute, hours.startHour, hours.startMinute)
    }

    fun onChannelChange(type: ChannelType, enabled: Boolean) {
        if (_state.value.isSaving || _state.value.isLoading) return
        val current = _state.value.preferences ?: return
        if (current.channels.firstOrNull { it.type == type }?.enabled == enabled) return
        val channels =
            if (current.channels.any { it.type == type }) {
                current.channels.map { if (it.type == type) it.copy(enabled = enabled) else it }
            } else {
                current.channels + NotificationChannel(type, enabled)
            }
        save(current.copy(channels = channels))
    }

    fun onPolicyChange(policy: NotificationPolicy, contacts: List<EmergencyContact>) {
        if (
            _state.value.isSaving ||
                _state.value.isLoading ||
                _state.value.preferences?.policy == null
        )
            return
        loadJob?.cancel()
        _state.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            when (
                val result =
                    updatePolicy(UpdateNotificationPolicyCommand(userId, policy, contacts)).also {
                        currentCoroutineContext().ensureActive()
                    }
            ) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            preferences = result.value,
                            message = NotificationMessage.Saved,
                        )
                    }
                is AppResult.Failure ->
                    _state.update { it.copy(isSaving = false, message = notificationErrorMessage(result)) }
            }
        }
    }

    private fun changeQuietHours(
        current: NotificationPreferences,
        endHour: Int,
        endMinute: Int,
        startHour: Int,
        startMinute: Int,
    ) {
        val hours =
            try {
                QuietHours(startHour, startMinute, endHour, endMinute)
            } catch (exception: IllegalArgumentException) {
                _state.update { it.copy(message = NotificationMessage.ErrorInvalidHours) }
                return
            }
        save(current.copy(quietHours = hours))
    }

    private fun save(updated: NotificationPreferences) {
        if (_state.value.isSaving) return
        loadJob?.cancel()
        _state.update { it.copy(isLoading = false, isSaving = true, message = null) }
        viewModelScope.launch {
            val command =
                UpdateNotificationPreferencesCommand(userId, updated.quietHours, updated.channels)
            when (
                val result =
                    updatePreferences(command).also { currentCoroutineContext().ensureActive() }
            ) {
                is AppResult.Success ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            preferences = result.value,
                            message = NotificationMessage.Saved,
                        )
                    }

                is AppResult.Failure ->
                    _state.update { it.copy(isSaving = false, message = notificationErrorMessage(result)) }
            }
        }
    }


    /** Constructs the notification settings model for one authenticated user. */
    class Factory(
        private val userId: String,
        private val getPreferences: GetNotificationPreferencesQueryHandler,
        private val updatePreferences: UpdateNotificationPreferencesCommandHandler,
        private val updatePolicy: UpdateNotificationPolicyCommandHandler,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(NotificationPreferencesViewModel::class.java))
            return NotificationPreferencesViewModel(
                userId,
                getPreferences,
                updatePreferences,
                updatePolicy,
            )
                as T
        }
    }
}
