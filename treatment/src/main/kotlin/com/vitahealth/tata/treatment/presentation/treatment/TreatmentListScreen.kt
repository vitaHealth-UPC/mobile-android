package com.vitahealth.tata.treatment.presentation.treatment
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import com.vitahealth.tata.treatment.domain.model.TreatmentStatus
@Composable
fun TreatmentListRoute(factory: TreatmentListViewModel.Factory,olderAdultName: String,onOpen: (String)->Unit,onAddMedication: ()->Unit,onBack: ()->Unit) {
    val vm: TreatmentListViewModel=viewModel(factory=factory);val state by vm.state.collectAsState()
    Column(Modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState()).padding(horizontal=22.dp,vertical=28.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("Tratamientos de $olderAdultName",fontFamily=FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),fontSize=29.sp,color=tataTextColor())
        Text("Gestiona la pauta sin perder historial.",fontSize=12.sp,color=tataMutedColor())
        if(state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let{TataCard{Text(if(it=="CARE_LINK_NOT_AUTHORIZED") "Necesitas un vínculo de cuidado activo." else "No pudimos cargar los tratamientos.",color=TataError);TataButton("Reintentar",vm::refresh)}}
        if(!state.loading && state.error==null && state.treatments.isEmpty()) TataCard{Text("Aún no hay tratamientos registrados.",color=tataMutedColor())}
        state.treatments.forEach{treatment->TataCard {
            Text(treatment.name,fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=tataTextColor())
            Text(when(treatment.status){TreatmentStatus.ACTIVE->"Activo";TreatmentStatus.PAUSED->"Pausado";TreatmentStatus.DRAFT->"Borrador"},color=TataNavy)
            treatment.dose?.let{Text(it,color=tataTextColor())}
            if(treatment.scheduledTimes.isNotEmpty()) Text(treatment.scheduledTimes.joinToString(" · "),fontSize=12.sp,color=tataMutedColor())
            TataButton("Ver detalle",{onOpen(treatment.id)},style=TataButtonStyle.Secondary)
        }}
        TataCard(containerColor=TataCream){Text("Historial protegido",fontWeight=FontWeight.SemiBold,color=tataTextColor());Text("Pausar o editar solo afecta las tomas futuras.",fontSize=12.sp,color=tataMutedColor())}
        TataButton("Agregar medicamento",onAddMedication)
        TextButton(onClick=onBack){Text("Volver al resumen")}
    }
}
