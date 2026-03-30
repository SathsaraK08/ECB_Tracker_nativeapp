package com.sathsara.ecbtracker.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TrackerDateTimeParserTest {

    @Test
    fun sanitizeDate_inserts_separators() {
        assertEquals("2026-03-29", TrackerDateTimeParser.sanitizeDate("20260329"))
    }

    @Test
    fun sanitizeTime_inserts_colon() {
        assertEquals("20:15", TrackerDateTimeParser.sanitizeTime("2015"))
    }

    @Test
    fun parseReminderTime_returns_hour_and_minute() {
        assertEquals(20 to 15, TrackerDateTimeParser.parseReminderTime("20:15"))
    }

    @Test
    fun parseReminderTime_returns_null_for_invalid_input() {
        assertNull(TrackerDateTimeParser.parseReminderTime("29:99"))
        assertNotNull(TrackerDateTimeParser.parseDate("2026-03-29"))
    }
}
