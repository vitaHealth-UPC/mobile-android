package com.vitahealth.tata.monitoring.presentation.alerts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vitahealth.tata.R
import com.vitahealth.tata.monitoring.presentation.alerts.ui.AlertFilters
import com.vitahealth.tata.monitoring.presentation.alerts.viewmodel.AlertFilter
import com.vitahealth.tata.shared.presentation.designsystem.theme.TataTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertFiltersTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun eachFilterHasAnAccessibleTouchTargetAndChangesSelection() {
        var selected by mutableStateOf(AlertFilter.ALL)
        compose.setContent { TataTheme { AlertFilters(selected) { selected = it } } }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        listOf(
                AlertFilter.PENDING to R.string.alerts_filter_pending,
                AlertFilter.UPCOMING to R.string.alerts_filter_upcoming,
                AlertFilter.COMPLETED to R.string.alerts_filter_completed,
                AlertFilter.ALL to R.string.alerts_filter_all,
            )
            .forEach { (filter, label) ->
                compose
                    .onNodeWithText(context.getString(label))
                    .assertHeightIsAtLeast(48.dp)
                    .performClick()
                    .assertIsSelected()
                compose.runOnIdle { assertEquals(filter, selected) }
            }
    }
}
