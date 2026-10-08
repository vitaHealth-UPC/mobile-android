package com.vitahealth.tata.carelink.presentation.profiles
import androidx.lifecycle.*
import com.vitahealth.tata.carelink.application.*
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CaregiverProfilesState(val adults: List<LinkedAdult> = emptyList(),val name: String="",val birthDate: LocalDate?=null,val contactName: String="",val relationship: String="",val phone: String="",val showForm: Boolean=false,val busy: Boolean=false,val error: String?=null,val createdAdult: LinkedAdult?=null,val code: ProfileLinkingCode?=null)
class CaregiverProfilesViewModel(private val caregiverId: String,private val repository: CaregiverProfilesRepository): ViewModel() {
    private val mutable=MutableStateFlow(CaregiverProfilesState())
    val state=mutable.asStateFlow()
    init{refresh()}
    fun refresh(){ if(state.value.busy)return;mutable.update{it.copy(busy=true,error=null)};viewModelScope.launch{when(val result=repository.list(caregiverId)){is AppResult.Success->mutable.update{it.copy(adults=result.value,busy=false)};is AppResult.Failure->mutable.update{it.copy(busy=false,error=result.code)}}} }
    fun form(){mutable.update{it.copy(showForm=true,error=null)}}
    fun name(value: String){mutable.update{it.copy(name=value.take(120),error=null)}}
    fun date(value: LocalDate){mutable.update{it.copy(birthDate=value,error=null)}}
    fun contact(value: String){mutable.update{it.copy(contactName=value,error=null)}}
    fun relationship(value: String){mutable.update{it.copy(relationship=value,error=null)}}
    fun phone(value: String){mutable.update{it.copy(phone=value,error=null)}}
    fun save(){
        val value=state.value;if(value.busy)return
        val date=value.birthDate
        if(value.name.isBlank() || date==null || date.isAfter(LocalDate.now())){mutable.update{it.copy(error="VALIDATION_ERROR")};return}
        if(listOf(value.contactName,value.relationship,value.phone).any{it.isNotBlank()} && listOf(value.contactName,value.relationship,value.phone).any{it.isBlank()}){mutable.update{it.copy(error="CONTACT_INCOMPLETE")};return}
        mutable.update{it.copy(busy=true,error=null)}
        viewModelScope.launch{
            val adult=value.createdAdult ?: when(val result=repository.register(caregiverId,NewAdultProfile(value.name,date,value.contactName,value.relationship,value.phone))){is AppResult.Success->result.value;is AppResult.Failure->{mutable.update{it.copy(busy=false,error=result.code)};return@launch}}
            mutable.update{it.copy(createdAdult=adult)}
            when(val result=repository.linkingCode(caregiverId,adult)){is AppResult.Success->mutable.update{it.copy(busy=false,code=result.value,showForm=false)};is AppResult.Failure->mutable.update{it.copy(busy=false,error=result.code)}}
        }
    }
    class Factory(private val caregiverId: String,private val repository: CaregiverProfilesRepository): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T: ViewModel> create(modelClass: Class<T>): T=CaregiverProfilesViewModel(caregiverId,repository) as T
    }
}
