package com.sathsara.ecbtracker.logic

data class MeterReadingValidation(
    val reading: Double? = null,
    val errorMessage: String? = null
)

object MeterReadingParser {
    fun sanitize(input: String): String {
        return input.filter(Char::isDigit).take(7)
    }

    fun parse(input: String): Double? {
        if (input.length != 7 || input.any { !it.isDigit() }) {
            return null
        }

        val integerPart = input.substring(0, 5)
        val decimalPart = input.substring(5, 7)
        return "$integerPart.$decimalPart".toDoubleOrNull()
    }

    fun validate(input: String, previousReading: Double): MeterReadingValidation {
        val parsed = parse(input)
            ?: return MeterReadingValidation(errorMessage = "Enter a 7 digit meter reading.")

        if (parsed < previousReading) {
            return MeterReadingValidation(
                errorMessage = "Current reading must be equal to or greater than the previous reading."
            )
        }

        return MeterReadingValidation(reading = parsed)
    }
}
