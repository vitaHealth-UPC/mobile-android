package com.vitahealth.tata.monitoring.presentation.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.monitoring.domain.model.valueobjects.ContactChannelType
import com.vitahealth.tata.monitoring.presentation.contact.mappers.openContact
import com.vitahealth.tata.monitoring.presentation.summary.screens.FamilySummaryScreen
import com.vitahealth.tata.monitoring.presentation.summary.viewmodel.FamilySummaryViewModel

@Composable
fun FamilySummaryRoute(
    factory: FamilySummaryViewModel.Factory,
    olderAdultName: String,
    onAgenda: () -> Unit,
    onHistory: () -> Unit,
    onAddMedication: () -> Unit,
    onChangePerson: () -> Unit,
    onAccessibility: () -> Unit = {},
    onNotificationPreferences: () -> Unit = {},
    onMedications: () -> Unit = {},
    onTreatments: () -> Unit = {},
    onAlerts: () -> Unit = {},
    onSubscription: () -> Unit = {},
    onNotes: () -> Unit = {},
) {
    val model: FamilySummaryViewModel = viewModel(factory = factory)
    val state by model.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    FamilySummaryScreen(
        state,
        olderAdultName,
        model::refresh,
        onAgenda,
        onHistory,
        model::contact,
        onAlerts,
        onNotes,
        onAddMedication,
        onChangePerson,
        onAccessibility,
        onNotificationPreferences,
        onMedications,
        onTreatments,
        onSubscription,
    )
    state.dialog?.let { dialog ->
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = model::dismissDialog,
            title = { Text(dialog.title) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (dialog.loading) Text("Consultando…")
                    else if (dialog.rows.isEmpty()) Text("No hay registros para mostrar.")
                    else
                        dialog.rows.forEach {
                            Text(it, modifier = Modifier.padding(bottom = 16.dp))
                        }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialog.contact?.let { context.openContact(it) }
                        model.dismissDialog()
                    }
                ) {
                    Text(
                        when (dialog.contact?.type) {
                            null -> "Cerrar"
                            ContactChannelType.PHONE -> "Abrir teléfono"
                            ContactChannelType.WHATSAPP -> "Abrir WhatsApp"
                        }
                    )
                }
            },
        )
    }
}
