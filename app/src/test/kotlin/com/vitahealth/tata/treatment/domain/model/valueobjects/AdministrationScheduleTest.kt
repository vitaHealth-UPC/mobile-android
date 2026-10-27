package com.vitahealth.tata.treatment.domain.model.valueobjects

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AdministrationScheduleTest {
    @Test
    fun scheduleKeepsItsNormalizedTimesWhenCallerMutatesInput() {
        val morning = LocalTime.of(8, 0)
        val evening = LocalTime.of(20, 0)
        val input = mutableListOf(evening, morning, morning)
        val schedule = requireNotNull(AdministrationSchedule.create(input))
        input.clear()

        assertEquals(listOf(morning, evening), schedule.times)
        assertThrows(UnsupportedOperationException::class.java) {
            (schedule.times as MutableList<LocalTime>).clear()
        }
        assertEquals(listOf(morning, evening), schedule.times)
        assertEquals(AdministrationSchedule.create(listOf(evening, morning)), schedule)
    }

    @Test
    fun emptyScheduleIsRejected() {
        assertNull(AdministrationSchedule.create(emptyList()))
    }
}
