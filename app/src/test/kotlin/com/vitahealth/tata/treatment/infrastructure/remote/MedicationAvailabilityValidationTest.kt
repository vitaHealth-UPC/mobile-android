package com.vitahealth.tata.treatment.infrastructure.remote

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationAvailabilityValidationTest {
    private val json =
        """{"id":"medication","olderAdultId":"adult","name":"Losartán","presentation":"50 mg"%s}"""

    @Test
    fun missingOrNullAvailabilityCannotBecomeAnInactiveMedication() {
        for (field in listOf("", ",\"active\":null")) {
            val response = Gson().fromJson(json.format(field), MedicationResponse::class.java)
            assertThrows(IllegalArgumentException::class.java) { response.toDomain() }
        }
    }

    @Test
    fun explicitActiveAndInactiveValuesRemainValid() {
        val active =
            Gson().fromJson(json.format(",\"active\":true"), MedicationResponse::class.java)
        val inactive =
            Gson().fromJson(json.format(",\"active\":false"), MedicationResponse::class.java)
        assertTrue(active.toDomain().active)
        assertFalse(inactive.toDomain().active)
    }
}
