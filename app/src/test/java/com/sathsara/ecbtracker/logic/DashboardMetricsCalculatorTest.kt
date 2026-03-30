package com.sathsara.ecbtracker.logic

import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DashboardMetricsCalculatorTest {

    @Test
    fun build_uses_defaults_when_settings_missing() {
        val metrics = DashboardMetricsCalculator.build(
            username = null,
            settings = null,
            monthEntries = emptyList(),
            today = LocalDate.of(2026, 3, 29)
        )

        assertEquals("User", metrics.username)
        assertEquals("Add account number", metrics.accountNumber)
        assertEquals(32.0, metrics.ratePerUnit, 0.0001)
    }

    @Test
    fun build_calculates_monthly_today_and_average_values() {
        val entries = listOf(
            Entry(date = "2026-03-28", used = 4.5),
            Entry(date = "2026-03-29", used = 5.0),
            Entry(date = "2026-03-29", used = 2.0)
        )

        val metrics = DashboardMetricsCalculator.build(
            username = "Sathsara",
            settings = UserSettings(lkrPerUnit = 40.0, accountNumber = "123456789"),
            monthEntries = entries,
            today = LocalDate.of(2026, 3, 29)
        )

        assertEquals("Sathsara", metrics.username)
        assertEquals("123456789", metrics.accountNumber)
        assertEquals(11.5, metrics.monthlyKwh, 0.0001)
        assertEquals(7.0, metrics.todayKwh, 0.0001)
        assertEquals(11.5, metrics.weeklyKwh, 0.0001)
        assertEquals(11.5 / 29.0, metrics.averageDailyKwh, 0.0001)
        assertEquals(460.0, metrics.currentBill, 0.0001)
        assertEquals((11.5 / 29.0) * 31.0 * 40.0, metrics.projectedBill, 0.0001)
        assertEquals(2, metrics.chartData.size)
    }
}
