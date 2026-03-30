package com.sathsara.ecbtracker.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduleCalculatorTest {

    @Test
    fun nextTriggerMillis_returns_same_day_when_time_is_still_ahead() {
        val now = ZonedDateTime.of(2026, 3, 29, 18, 0, 0, 0, ZoneId.of("Asia/Colombo"))

        val trigger = ReminderScheduleCalculator.nextTriggerMillis(
            now = now,
            hour = 20,
            minute = 0
        )

        val expected = ZonedDateTime.of(2026, 3, 29, 20, 0, 0, 0, ZoneId.of("Asia/Colombo"))
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, trigger)
    }

    @Test
    fun nextTriggerMillis_rolls_to_next_day_when_time_has_passed() {
        val now = ZonedDateTime.of(2026, 3, 29, 21, 15, 0, 0, ZoneId.of("Asia/Colombo"))

        val trigger = ReminderScheduleCalculator.nextTriggerMillis(
            now = now,
            hour = 20,
            minute = 0
        )

        val expected = ZonedDateTime.of(2026, 3, 30, 20, 0, 0, 0, ZoneId.of("Asia/Colombo"))
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, trigger)
    }
}
