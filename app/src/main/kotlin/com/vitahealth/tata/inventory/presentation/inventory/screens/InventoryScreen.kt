package com.vitahealth.tata.inventory.presentation.inventory.screens

import com.vitahealth.tata.inventory.presentation.inventory.viewmodel.InventoryUiState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitahealth.tata.R
import com.vitahealth.tata.inventory.presentation.inventory.ui.ErrorBody
import com.vitahealth.tata.inventory.presentation.inventory.ui.LoadingBody
import com.vitahealth.tata.inventory.presentation.inventory.ui.NotInitializedBody
import com.vitahealth.tata.inventory.presentation.inventory.ui.ReadyBody
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTab
import com.vitahealth.tata.shared.presentation.designsystem.components.CaregiverTabBar
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataMutedColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataTextColor
import com.vitahealth.tata.shared.presentation.designsystem.theme.tataBackgroundColor

/** Renders all Figma-approved inventory states from ViewModel-owned UI state. */
@Composable
fun InventoryScreen(
    state: InventoryUiState,
    onInitialQuantityChange: (String) -> Unit,
    onThresholdChange: (String) -> Unit,
    onDefineInitial: () -> Unit,
    onReplenishmentChange: (String) -> Unit,
    onSaveReplenishment: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onLotChange: (String) -> Unit = {},
    olderAdultName: String = "",
    onTabSelected: (CaregiverTab) -> Unit = {},
    onSignIn: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier.fillMaxSize().background(tataBackgroundColor())) {
        Column(
            modifier =
                Modifier.weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 28.dp, bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.inventory_title),
                color = tataTextColor(),
                fontSize = 29.sp,
                lineHeight = 38.sp,
                fontFamily = FontFamily(Font(R.font.tata_serif)),
                modifier = Modifier.padding(start = 2.dp),
            )
            Text(
                text =
                    if (olderAdultName.isBlank()) stringResource(R.string.inventory_subtitle)
                    else
                        stringResource(
                            R.string.inventory_subtitle_for_person,
                            olderAdultName.substringBefore(' '),
                        ),
                color = tataMutedColor(),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily(Font(R.font.tata_inter)),
                modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 20.dp),
            )

            when (state) {
                is InventoryUiState.Loading -> LoadingBody()
                is InventoryUiState.NotInitialized ->
                    NotInitializedBody(
                        state = state,
                        onInitialQuantityChange = onInitialQuantityChange,
                        onThresholdChange = onThresholdChange,
                        onDefineInitial = onDefineInitial,
                    )
                is InventoryUiState.Ready ->
                    ReadyBody(
                        state = state,
                        onReplenishmentChange = onReplenishmentChange,
                        onSaveReplenishment = onSaveReplenishment,
                        onLotChange = onLotChange,
                    )
                is InventoryUiState.Error -> ErrorBody(state, onRetry, onSignIn, onBack)
            }
        }
        CaregiverTabBar(CaregiverTab.Person, onTabSelected)
    }
}
