package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetScheduleInstructionsCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class SetScheduleInstructionsCommandHandlerTest {
    private val handler = SetScheduleInstructionsCommandHandler()

    @Test
    fun multipleTimesAreParsedDeduplicatedAndSorted() {
        val result = handler(
            SetScheduleInstructionsCommand(
                scheduleText = "20:00, 08:00, 20:00, 13:00",
                instructions = " Con agua ",
            ),
        )

        assertTrue(result is AppResult.Success)
        val value = (result as AppResult.Success).value
        assertEquals(
            listOf(LocalTime.of(8, 0), LocalTime.of(13, 0), LocalTime.of(20, 0)),
            value.schedule.times,
        )
        assertEquals("Con agua", value.instructions.value)
    }

    @Test
    fun emptyScheduleIsRejected() {
        val result = handler(
            SetScheduleInstructionsCommand(
                scheduleText = " ",
                instructions = "",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("EMPTY_SCHEDULE", (result as AppResult.Failure).code)
    }

    @Test
    fun invalidTimeIsRejected() {
        val result = handler(
            SetScheduleInstructionsCommand(
                scheduleText = "08:00, 25:00",
                instructions = "",
            ),
        )

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_SCHEDULE_TIME", (result as AppResult.Failure).code)
    }
}
