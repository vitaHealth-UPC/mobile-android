package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetDoseFrequencyCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetDoseFrequencyCommandHandlerTest {
    private val handler = SetDoseFrequencyCommandHandler()

    @Test
    fun validValuesAreTrimmedAndWrappedAsDomainValueObjects() {
        val result = handler(
            SetDoseFrequencyCommand(
                dosage = " 1 comprimido ",
                frequency = " Cada día ",
            ),
        )

        assertTrue(result is AppResult.Success)
        val basics = (result as AppResult.Success).value
        assertEquals("1 comprimido", basics.dosage.value)
        assertEquals("Cada día", basics.frequency.value)
    }

    @Test
    fun blankDosageIsRejected() {
        val result = handler(
            SetDoseFrequencyCommand(
                dosage = " ",
                frequency = "Cada día",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_DOSAGE", (result as AppResult.Failure).code)
    }

    @Test
    fun blankFrequencyIsRejected() {
        val result = handler(
            SetDoseFrequencyCommand(
                dosage = "1 comprimido",
                frequency = " ",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_FREQUENCY", (result as AppResult.Failure).code)
    }
}
