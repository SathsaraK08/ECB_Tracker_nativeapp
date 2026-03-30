package com.sathsara.ecbtracker.logic

import com.sathsara.ecbtracker.data.model.Entry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UsageForecastEngineTest {

    @Test
    fun build_returns_startup_tip_when_no_entries_exist() {
        val forecast = UsageForecastEngine.build(
            entries = emptyList(),
            ratePerUnit = 32.0,
            today = LocalDate.of(2026, 3, 29)
        )

        assertEquals(0.0, forecast.projectedBill, 0.0001)
        assertTrue(forecast.tips.first().title.contains("Start logging"))
    }

    @Test
    fun build_identifies_peak_period_and_top_appliance() {
        val forecast = UsageForecastEngine.build(
            entries = listOf(
                Entry(date = "2026-03-28", time = "18:10", used = 7.0, appliances = listOf("A/C", "Rice Cooker")),
                Entry(date = "2026-03-29", time = "18:30", used = 5.0, appliances = listOf("A/C")),
                Entry(date = "2026-03-29", time = "08:00", used = 2.0, appliances = listOf("Kettle"))
            ),
            ratePerUnit = 40.0,
            today = LocalDate.of(2026, 3, 29)
        )

        assertEquals("Evening", forecast.peakHours)
        assertTrue(forecast.projectedBill > 0.0)
        assertTrue(forecast.tips.any { it.title.contains("A/C") })
    }
}
