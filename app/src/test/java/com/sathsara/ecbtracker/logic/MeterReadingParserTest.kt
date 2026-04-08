package com.sathsara.ecbtracker.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MeterReadingParserTest {

    @Test
    fun sanitize_keeps_only_digits_and_limits_length() {
        assertEquals("1234567", MeterReadingParser.sanitize("12a34-56789"))
    }

    @Test
    fun parse_returns_reading_when_input_has_seven_digits() {
        val parsed = MeterReadingParser.parse("1234567")
        assertNotNull(parsed)
        assertEquals(12345.67, parsed!!, 0.0001)
    }

    @Test
    fun validate_rejects_short_input() {
        val result = MeterReadingParser.validate("1234", previousReading = 10.0)

        assertNull(result.reading)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun validate_rejects_readings_below_previous_value() {
        val result = MeterReadingParser.validate("0009999", previousReading = 100.0)

        assertNull(result.reading)
        assertEquals(
            "Current reading must be equal to or greater than the previous reading.",
            result.errorMessage
        )
    }
}
