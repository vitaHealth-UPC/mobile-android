package com.vitahealth.tata.carelink.presentation.profiles
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.carelink.application.*
import com.vitahealth.tata.shared.design.components.*
import com.vitahealth.tata.shared.design.theme.*
import java.time.*

@Composable
fun CaregiverProfilesRoute(factory: CaregiverProfilesViewModel.Factory,onOpenAdult: (LinkedAdult)->Unit,onAdultConsent: (ProfileLinkingCode)->Unit,onSignOut: ()->Unit) {
    val vm: CaregiverProfilesViewModel=viewModel(factory=factory)
    val state by vm.state.collectAsState()
    CaregiverProfilesScreen(state,vm::form,vm::name,vm::date,vm::contact,vm::relationship,vm::phone,vm::save,vm::refresh,onOpenAdult,onAdultConsent,onSignOut)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProfilesScreen(state: CaregiverProfilesState,onForm: ()->Unit,onName: (String)->Unit,onDate: (LocalDate)->Unit,onContact: (String)->Unit,onRelationship: (String)->Unit,onPhone: (String)->Unit,onSave: ()->Unit,onRefresh: ()->Unit,onOpenAdult: (LinkedAdult)->Unit,onAdultConsent: (ProfileLinkingCode)->Unit,onSignOut: ()->Unit) {
    var dateDialog by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().background(TataSurface).verticalScroll(rememberScrollState()).padding(horizontal=22.dp,vertical=28.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("Personas vinculadas",fontFamily=FontFamily(Font(com.vitahealth.tata.shared.R.font.tata_serif)),fontSize=29.sp,color=tataTextColor())
        Text("Acompaña sus tratamientos y confirma el vínculo con su consentimiento.",fontSize=12.sp,color=tataMutedColor())
        if(state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let{Text(when(it){"CONTACT_INCOMPLETE"->"Completa los tres datos del contacto de emergencia.";"VALIDATION_ERROR"->"Completa el nombre y una fecha de nacimiento válida.";"NETWORK_UNAVAILABLE"->"No hay conexión. Inténtalo nuevamente.";else->"No pudimos completar la operación. Inténtalo nuevamente."},color=TataError)}
        state.adults.forEach{adult->TataCard(containerColor=TataMint){Text(adult.name,fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=tataTextColor());Text("Vínculo activo",fontSize=12.sp,color=tataMutedColor());TataButton("Ver resumen",{onOpenAdult(adult)},enabled=!state.busy)}}
        if(state.adults.isEmpty() && !state.showForm && state.code==null) TataCard{Text("Aún no tienes personas vinculadas.",color=tataMutedColor())}
        state.code?.let{link->TataCard(containerColor=TataLavender){Text(link.olderAdultName,fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=tataTextColor());Text("Código temporal",color=tataMutedColor());Text(link.code,fontSize=28.sp,fontWeight=FontWeight.Bold,color=TataNavy);Text("Comparte este código con el adulto mayor para que revise y acepte el consentimiento. Vence en 15 minutos.",fontSize=12.sp,color=tataMutedColor());TataButton("Continuar con el adulto mayor",{onAdultConsent(link)},enabled=!state.busy);TataButton("Renovar código",onSave,style=TataButtonStyle.Secondary,enabled=!state.busy)}}
        if(state.showForm){
            TataFormField("Nombre completo",state.name,onName,enabled=!state.busy && state.createdAdult==null)
            TextButton(onClick={dateDialog=true},enabled=!state.busy && state.createdAdult==null){Text(state.birthDate?.toString() ?: "Seleccionar fecha de nacimiento")}
            TataFormField("Contacto de emergencia (opcional)",state.contactName,onContact,enabled=!state.busy && state.createdAdult==null)
            TataFormField("Parentesco",state.relationship,onRelationship,enabled=!state.busy && state.createdAdult==null)
            TataFormField("Teléfono",state.phone,onPhone,enabled=!state.busy && state.createdAdult==null)
            TataButton(if(state.createdAdult==null) "Guardar y generar vínculo" else "Generar código",onSave,enabled=!state.busy)
        } else if(state.code==null) TataButton("Registrar adulto mayor",onForm,enabled=!state.busy)
        TextButton(onClick=onRefresh,enabled=!state.busy){Text("Actualizar vínculos")}
        TextButton(onClick=onSignOut,enabled=!state.busy){Text("Cerrar sesión")}
    }
    if(dateDialog){val picker=rememberDatePickerState(initialSelectedDateMillis=state.birthDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(onDismissRequest={dateDialog=false},confirmButton={TextButton(onClick={picker.selectedDateMillis?.let{onDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())};dateDialog=false}){Text("Aceptar")}},dismissButton={TextButton(onClick={dateDialog=false}){Text("Cancelar")}}){DatePicker(state=picker)}
    }
}
