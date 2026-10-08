package com.vitahealth.tata.treatment.application.handlers

import com.vitahealth.tata.shared.common.result.AppResult
import com.vitahealth.tata.treatment.application.commands.SetReminderPolicyCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetReminderPolicyCommandHandlerTest {
    private val handler = SetReminderPolicyCommandHandler()

    @Test
    fun validDelayIsWrappedAsReminderPolicy() {
        val result = handler(SetReminderPolicyCommand(followUpDelayMinutes = " 10 "))

        assertTrue(result is AppResult.Success)
        val policy = (result as AppResult.Success).value
        assertEquals(10, policy.followUpDelayMinutes.value)
    }

    @Test
    fun negativeDelayIsRejected() {
        val result = handler(SetReminderPolicyCommand(followUpDelayMinutes = "-1"))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_REMINDER_DELAY", (result as AppResult.Failure).code)
    }

    @Test
    fun delayBeyondOneDayIsRejected() {
        val result = handler(SetReminderPolicyCommand(followUpDelayMinutes = "1441"))

        assertTrue(result is AppResult.Failure)
        assertEquals("INVALID_REMINDER_DELAY", (result as AppResult.Failure).code)
    }
}
