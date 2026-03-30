package com.sathsara.ecbtracker.logic

import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.ForecastResponse
import com.sathsara.ecbtracker.data.model.ForecastTip
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

object UsageForecastEngine {
    fun build(
        entries: List<Entry>,
        ratePerUnit: Double,
        today: LocalDate = LocalDate.now()
    ): ForecastResponse {
        if (entries.isEmpty()) {
            return ForecastResponse(
                projectedBill = 0.0,
                efficiencyRating = "Medium",
                peakHours = "Not enough data yet",
                tips = listOf(
                    ForecastTip(
                        title = "Start logging twice a day",
                        description = "Morning and evening readings make the monthly prediction much more accurate.",
                        savingLkr = 0.0
                    )
                )
            )
        }

        val monthlyUsage = entries.sumOf { it.used }
        val daysElapsed = today.dayOfMonth.coerceAtLeast(1)
        val daysInMonth = YearMonth.from(today).lengthOfMonth()
        val averageDailyUsage = monthlyUsage / daysElapsed
        val projectedBill = averageDailyUsage * daysInMonth * ratePerUnit

        val peakBucket = entries
            .groupBy { bucketFor(it.time) }
            .maxByOrNull { (_, values) -> values.sumOf { entry -> entry.used } }
            ?.key
            ?: "Not enough data yet"

        val applianceTip = buildApplianceTip(entries, ratePerUnit)
        val peakTip = ForecastTip(
            title = "Shift heavy use away from $peakBucket",
            description = "Your readings are highest during $peakBucket. Moving A/C, water heating, or cooking loads away from that window can smooth the bill.",
            savingLkr = (averageDailyUsage * ratePerUnit * 2.0).coerceAtLeast(0.0)
        )
        val baselineTip = ForecastTip(
            title = "Log standby and overnight loads",
            description = "If the meter keeps climbing at night, check always-on items like routers, chargers, fridge seals, and water pumps.",
            savingLkr = (ratePerUnit * 8.0).coerceAtLeast(0.0)
        )

        return ForecastResponse(
            projectedBill = projectedBill,
            efficiencyRating = when {
                averageDailyUsage <= 5.0 -> "High"
                averageDailyUsage <= 10.0 -> "Medium"
                else -> "Low"
            },
            peakHours = peakBucket,
            tips = listOfNotNull(applianceTip, peakTip, baselineTip).take(3)
        )
    }

    private fun buildApplianceTip(entries: List<Entry>, ratePerUnit: Double): ForecastTip? {
        val highUsageEntries = entries.filter { it.used > 0.0 }
            .sortedByDescending { it.used }
            .take(maxOf(1, entries.size / 3))

        val topAppliance = highUsageEntries
            .flatMap { entry -> entry.appliances.orEmpty() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: return null

        val suggestion = when {
            topAppliance.contains("a/c", ignoreCase = true) ->
                "The A/C appears in your heavier readings. Raise the thermostat slightly, close doors, and shorten runtime when possible."
            topAppliance.contains("water", ignoreCase = true) || topAppliance.contains("heater", ignoreCase = true) ->
                "Water heating shows up in your heavier readings. Shorter heating cycles and timer-based use can bring the bill down."
            topAppliance.contains("oven", ignoreCase = true) || topAppliance.contains("kettle", ignoreCase = true) ->
                "Cooking appliances appear often in higher-usage periods. Batch cooking and avoiding reheating several times can help."
            else ->
                "$topAppliance appears often in higher-usage readings. Reducing its runtime is the clearest place to start."
        }

        return ForecastTip(
            title = "Watch $topAppliance",
            description = suggestion,
            savingLkr = (ratePerUnit * 10.0).coerceAtLeast(0.0)
        )
    }

    private fun bucketFor(time: String): String {
        val localTime = runCatching { LocalTime.parse(time) }.getOrNull() ?: return "Unknown"
        return when {
            localTime.hour in 5..11 -> "Morning"
            localTime.hour in 12..16 -> "Afternoon"
            localTime.hour in 17..21 -> "Evening"
            else -> "Night"
        }
    }
}
