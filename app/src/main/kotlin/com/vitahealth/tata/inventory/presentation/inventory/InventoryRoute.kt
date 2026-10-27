package com.vitahealth.tata.inventory.presentation.inventory

import com.vitahealth.tata.inventory.presentation.inventory.screens.InventoryScreen
import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab

/** Binds the inventory ViewModel to the existing screen without exposing infrastructure. */
@Composable
fun InventoryRoute(
    factory: InventoryViewModel.Factory,
    modifier: Modifier = Modifier,
    olderAdultName: String = "",
    onTabSelected: (CaregiverTab) -> Unit = {},
    onSignIn: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    val viewModel: InventoryViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    InventoryScreen(
        state = state,
        onInitialQuantityChange = viewModel::onInitialQuantityChange,
        onThresholdChange = viewModel::onThresholdChange,
        onDefineInitial = viewModel::defineInitialInventory,
        onReplenishmentChange = viewModel::onReplenishmentQuantityChange,
        onSaveReplenishment = viewModel::saveReplenishment,
        onLotChange = viewModel::onLotChange,
        onRetry = viewModel::retry,
        onSignIn = onSignIn,
        onBack = onBack,
        modifier = modifier,
        olderAdultName = olderAdultName,
        onTabSelected = onTabSelected,
    )
}
