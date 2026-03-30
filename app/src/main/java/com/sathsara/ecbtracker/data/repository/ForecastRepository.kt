package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.BuildConfig
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.model.ForecastResponse
import com.sathsara.ecbtracker.data.service.GeminiService
import com.sathsara.ecbtracker.logic.UsageForecastEngine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastRepository @Inject constructor(
    private val geminiService: GeminiService,
    private val entryRepository: EntryRepositoryContract,
    private val settingsRepository: SettingsRepositoryContract,
    private val dataStoreManager: DataStoreManager
) {
    private val json = Json { prettyPrint = false }

    suspend fun getMonthlyForecast(
        yearMonth: String,
        today: LocalDate = LocalDate.now()
    ): Result<ForecastResponse> {
        return Result.runCatching {
            val entries = entryRepository.getEntriesForMonth(yearMonth).getOrNull().orEmpty()
            val ratePerUnit = settingsRepository.getSettings().getOrNull()?.lkrPerUnit ?: 32.0
            val localForecast = UsageForecastEngine.build(
                entries = entries,
                ratePerUnit = ratePerUnit,
                today = today
            )

            val apiKeyOverride = dataStoreManager.geminiApiKey.first()
            val apiKey = apiKeyOverride.ifBlank { BuildConfig.GEMINI_API_KEY }
            val currencyCode = dataStoreManager.currencyCode.first()

            if (apiKey.isBlank() || entries.isEmpty()) {
                return@runCatching localForecast
            }

            val simplifiedEntries = entries.map {
                mapOf(
                    "date" to it.date,
                    "time" to it.time,
                    "used" to it.used,
                    "appliances" to it.appliances.orEmpty(),
                    "note" to it.note.orEmpty()
                )
            }

            val entriesJson = json.encodeToString(simplifiedEntries)
            geminiService.getForecast(
                entriesJson = entriesJson,
                lkrPerUnit = ratePerUnit,
                currencyCode = currencyCode,
                apiKey = apiKey
            ).getOrElse { localForecast }
        }
    }
}
