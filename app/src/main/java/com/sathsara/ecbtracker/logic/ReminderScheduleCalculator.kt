package com.sathsara.ecbtracker.logic

import java.time.ZoneId
import java.time.ZonedDateTime

object ReminderScheduleCalculator {
    fun nextTriggerMillis(
        now: ZonedDateTime = ZonedDateTime.now(),
        hour: Int,
        minute: Int,
        zoneId: ZoneId = now.zone
    ): Long {
        val normalizedNow = now.withZoneSameInstant(zoneId)
        val scheduledToday = normalizedNow
            .withHour(hour.coerceIn(0, 23))
            .withMinute(minute.coerceIn(0, 59))
            .withSecond(0)
            .withNano(0)

        val nextTrigger = if (scheduledToday.isAfter(normalizedNow)) {
            scheduledToday
        } else {
            scheduledToday.plusDays(1)
        }

        return nextTrigger.toInstant().toEpochMilli()
    }
}
