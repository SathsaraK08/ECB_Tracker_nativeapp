package com.sathsara.ecbtracker.logic

import java.time.LocalDate
import java.time.LocalTime

object TrackerDateTimeParser {
    fun sanitizeDate(input: String): String {
        val digits = input.filter(Char::isDigit).take(8)
        return buildString {
            digits.take(4).forEach(::append)
            if (digits.length > 4) {
                append('-')
                digits.substring(4, minOf(6, digits.length)).forEach(::append)
            }
            if (digits.length > 6) {
                append('-')
                digits.substring(6, digits.length).forEach(::append)
            }
        }
    }

    fun sanitizeTime(input: String): String {
        val digits = input.filter(Char::isDigit).take(4)
        return if (digits.length <= 2) {
            digits
        } else {
            "${digits.take(2)}:${digits.drop(2)}"
        }
    }

    fun parseDate(input: String): LocalDate? = runCatching {
        LocalDate.parse(input)
    }.getOrNull()

    fun parseTime(input: String): LocalTime? = runCatching {
        LocalTime.parse(input)
    }.getOrNull()

    fun parseReminderTime(input: String): Pair<Int, Int>? {
        val parsed = parseTime(input) ?: return null
        return parsed.hour to parsed.minute
    }

    fun formatTime(hour: Int, minute: Int): String {
        return "%02d:%02d".format(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
}
