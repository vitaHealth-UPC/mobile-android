package com.vitahealth.tata.inventory.presentation.navigation

import android.net.Uri

/** Defines the inventory entry contract independently of the application's other destinations. */
object InventoryDestination {
    const val medicationIdArgument = "medicationId"
    const val medicationNameArgument = "medicationName"
    const val unitArgument = "unit"

    val route: String =
        "inventory/{$medicationIdArgument}/{$medicationNameArgument}/{$unitArgument}"

    /** Encodes path segments and preserves empty display labels as nonempty route arguments. */
    fun createRoute(medicationId: String, medicationName: String, unit: String): String =
        "inventory/" +
            Uri.encode(medicationId) +
            "/" +
            Uri.encode(medicationName.ifBlank { " " }) +
            "/" +
            Uri.encode(unit.ifBlank { " " })
}
