package com.vitahealth.tata.treatment.presentation.treatment
import androidx.lifecycle.*
import com.vitahealth.tata.treatment.application.handlers.ListTreatmentsQueryHandler
import com.vitahealth.tata.treatment.application.queries.ListTreatmentsQuery
import com.vitahealth.tata.treatment.application.readmodels.TreatmentDetailReadModel
import com.vitahealth.tata.shared.common.result.AppResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TreatmentListState(val treatments: List<TreatmentDetailReadModel> = emptyList(),val loading: Boolean=true,val error: String?=null)
class TreatmentListViewModel(private val caregiverId: String,private val olderAdultId: String,private val query: ListTreatmentsQueryHandler): ViewModel() {
    private val mutable=MutableStateFlow(TreatmentListState());val state=mutable.asStateFlow()
    init{refresh()}
    fun refresh(){mutable.update{it.copy(loading=true,error=null)};viewModelScope.launch{when(val result=query(ListTreatmentsQuery(caregiverId,olderAdultId))){is AppResult.Success->mutable.value=TreatmentListState(result.value,false);is AppResult.Failure->mutable.update{it.copy(loading=false,error=result.code)}}}}
    class Factory(private val caregiverId: String,private val olderAdultId: String,private val query: ListTreatmentsQueryHandler): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST") override fun <T: ViewModel> create(modelClass: Class<T>): T=TreatmentListViewModel(caregiverId,olderAdultId,query) as T
    }
}
