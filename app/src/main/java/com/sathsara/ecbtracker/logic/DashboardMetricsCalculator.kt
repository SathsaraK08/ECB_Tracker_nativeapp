package com.sathsara.ecbtracker.logic

import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.UserSettings
import java.time.LocalDate
import java.time.YearMonth

data class DashboardMetrics(
    val username: String,
    val accountNumber: String,
    val monthlyKwh: Double,
    val todayKwh: Double,
    val weeklyKwh: Double,
    val averageDailyKwh: Double,
    val currentBill: Double,
    val projectedBill: Double,
    val ratePerUnit: Double,
    val chartData: List<Pair<String, Float>>
)

object DashboardMetricsCalculator {
    fun build(
        username: String?,
        settings: UserSettings?,
        monthEntries: List<Entry>,
        today: LocalDate = LocalDate.now()
    ): DashboardMetrics {
        val monthlyKwh = monthEntries.sumOf { it.used }
        val todayKwh = monthEntries.filter { it.date == today.toString() }.sumOf { it.used }
        val weeklyStart = today.minusDays(6)
        val weeklyKwh = monthEntries.filter {
            runCatching { LocalDate.parse(it.date) }.getOrNull()?.let { entryDate ->
                !entryDate.isBefore(weeklyStart) && !entryDate.isAfter(today)
            } ?: false
        }.sumOf { it.used }
        val rate = settings?.lkrPerUnit?.takeIf { it > 0.0 } ?: 32.0
        val totalDays = YearMonth.from(today).lengthOfMonth().coerceAtLeast(1)
        val elapsedDays = today.dayOfMonth.coerceAtLeast(1)
        val averageDailyKwh = monthlyKwh / elapsedDays
        val grouped = monthEntries
            .groupBy { it.date }
            .toSortedMap()
            .entries
            .toList()
            .takeLast(7)
            .map { (date, entries) -> date.takeLast(2) to entries.sumOf { it.used }.toFloat() }
        val currentBill = monthlyKwh * rate
        val projectedBill = averageDailyKwh * totalDays * rate

        return DashboardMetrics(
            username = username?.takeIf { it.isNotBlank() } ?: "User",
            accountNumber = settings?.accountNumber?.takeIf { it.isNotBlank() } ?: "Add account number",
            monthlyKwh = monthlyKwh,
            todayKwh = todayKwh,
            weeklyKwh = weeklyKwh,
            averageDailyKwh = averageDailyKwh,
            currentBill = currentBill,
            projectedBill = projectedBill,
            ratePerUnit = rate,
            chartData = grouped
        )
    }
}
