package com.vitahealth.tata.treatment.application.commands

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommand
import com.vitahealth.tata.treatment.application.commands.ConfigureTreatmentCommandHandler
import com.vitahealth.tata.treatment.domain.model.entities.Treatment
import com.vitahealth.tata.treatment.domain.model.valueobjects.TreatmentStatus
import com.vitahealth.tata.treatment.domain.repositories.TreatmentLifecycleRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigureTreatmentCommandHandlerTest {
    private val valid =
        ConfigureTreatmentCommand(
            "caregiver",
            "treatment",
            "medication",
            " 1 comprimido ",
            " Diaria ",
            listOf("20:00", "08:00", "20:00"),
            " Con agua ",
            15,
        )

    @Test
    fun invalidDomainValuesNeverReachTheRepository() = runBlocking {
        var calls = 0
        val handler = ConfigureTreatmentCommandHandler(repository { calls++ })
        for (command in
            listOf(
                valid.copy(dose = "A".repeat(101)),
                valid.copy(frequency = "A".repeat(101)),
                valid.copy(instructions = "A".repeat(501)),
                valid.copy(scheduledTimes = listOf("24:00")),
                valid.copy(scheduledTimes = listOf("08:00:30")),
                valid.copy(scheduledTimes = emptyList()),
                valid.copy(reminderLeadMinutes = 1441),
            )) {
            assertEquals(
                "INVALID_TREATMENT_CONFIGURATION",
                (handler(command) as AppResult.Failure).code,
            )
        }
        assertEquals(0, calls)
    }

    @Test
    fun validDomainValuesAreNormalizedBeforeSubmission() = runBlocking {
        var submitted: Array<out Any?>? = null
        val handler = ConfigureTreatmentCommandHandler(repository { submitted = it })

        assertTrue(handler(valid) is AppResult.Success)

        assertEquals("1 comprimido", submitted!![3])
        assertEquals("Diaria", submitted!![4])
        assertEquals(listOf("08:00", "20:00"), submitted!![5])
        assertEquals("Con agua", submitted!![6])
    }

    private fun repository(onCall: (Array<out Any?>) -> Unit): TreatmentLifecycleRepository =
        Proxy.newProxyInstance(
            TreatmentLifecycleRepository::class.java.classLoader,
            arrayOf(TreatmentLifecycleRepository::class.java),
        ) { _, _, arguments ->
            onCall(arguments)
            AppResult.Success(Treatment("treatment", "adult", "Presión", TreatmentStatus.DRAFT))
        } as TreatmentLifecycleRepository
}
