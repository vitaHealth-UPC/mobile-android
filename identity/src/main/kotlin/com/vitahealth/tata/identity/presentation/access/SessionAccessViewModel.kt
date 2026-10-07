package com.vitahealth.tata.identity.presentation.access

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitahealth.tata.identity.application.SessionAccessRepository
import com.vitahealth.tata.identity.application.SessionSubject
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SessionAccessUiState(val email: String = "", val password: String = "", val pin: String = "", val busy: Boolean = false, val error: String? = null, val subject: SessionSubject? = null, val pinSaved: Boolean = false)
class SessionAccessViewModel(private val repository: SessionAccessRepository) : ViewModel() {
    private val mutable = MutableStateFlow(SessionAccessUiState())
    val state = mutable.asStateFlow()
    fun email(value: String) { mutable.update { it.copy(email=value,error=null) } }
    fun password(value: String) { mutable.update { it.copy(password=value,error=null) } }
    fun digit(value: String) { if (!state.value.busy) mutable.update { it.copy(pin=if(value=="⌫") it.pin.dropLast(1) else (it.pin+value).take(4),error=null) } }
    fun restore() = run { repository.current() }
    fun signIn() {
        val value=state.value
        if(value.email.isBlank() || value.password.isBlank()) { mutable.update { it.copy(error="REQUIRED_FIELDS") }; return }
        run { repository.signIn(value.email,value.password) }
    }
    fun pin(olderAdultId: String, setup: Boolean) {
        val value=state.value
        if(!value.pin.matches(Regex("[0-9]{4}"))) { mutable.update { it.copy(error="INVALID_PIN") }; return }
        if(setup) {
            if(value.busy) return
            mutable.update { it.copy(busy=true,error=null) }
            viewModelScope.launch {
                when(val result=repository.registerPin(olderAdultId,value.pin)) {
                    is AppResult.Success -> mutable.update { it.copy(busy=false,pin="",pinSaved=true) }
                    is AppResult.Failure -> mutable.update { it.copy(busy=false,pin="",error=result.code) }
                }
            }
        } else run { repository.signInWithPin(olderAdultId,value.pin) }
    }
    private fun run(call: suspend () -> AppResult<SessionSubject>) {
        if(state.value.busy) return
        mutable.update { it.copy(busy=true,error=null) }
        viewModelScope.launch {
            when(val result=call()) {
                is AppResult.Success -> mutable.update { it.copy(busy=false,password="",pin="",subject=result.value) }
                is AppResult.Failure -> mutable.update { it.copy(busy=false,pin="",error=if(result.code=="AUTHENTICATION_REQUIRED") null else result.code) }
            }
        }
    }
    class Factory(private val repository: SessionAccessRepository): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T: ViewModel> create(modelClass: Class<T>): T = SessionAccessViewModel(repository) as T
    }
}
