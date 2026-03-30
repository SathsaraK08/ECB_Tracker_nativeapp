package com.sathsara.ecbtracker.logic

object CurrencyFormatter {
    fun format(
        currencyCode: String,
        amount: Double,
        fractionDigits: Int = 0
    ): String {
        val safeCurrency = currencyCode.ifBlank { "LKR" }
        val pattern = if (fractionDigits <= 0) "%,.0f" else "%,.${fractionDigits}f"
        return "$safeCurrency ${pattern.format(amount)}"
    }
}
