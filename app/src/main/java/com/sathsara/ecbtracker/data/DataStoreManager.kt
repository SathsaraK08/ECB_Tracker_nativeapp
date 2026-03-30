package com.sathsara.ecbtracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ecb_settings")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val BILL_REMINDERS = booleanPreferencesKey("bill_reminders")
        val USAGE_ALERTS = booleanPreferencesKey("usage_alerts")
        val WEEKLY_DIGEST = booleanPreferencesKey("weekly_digest")
        val MONTHLY_EMAIL_REPORT = booleanPreferencesKey("monthly_email_report")
        val WEEKLY_SUMMARY = booleanPreferencesKey("weekly_summary")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_DARK_MODE] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_DARK_MODE] = enabled
        }
    }

    val billReminders: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BILL_REMINDERS] ?: true
    }

    suspend fun setBillReminders(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[BILL_REMINDERS] = enabled
        }
    }

    val usageAlerts: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USAGE_ALERTS] ?: true
    }

    suspend fun setUsageAlerts(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USAGE_ALERTS] = enabled
        }
    }

    val weeklyDigest: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[WEEKLY_DIGEST] ?: false
    }

    suspend fun setWeeklyDigest(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[WEEKLY_DIGEST] = enabled
        }
    }

    val monthlyEmailReport: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MONTHLY_EMAIL_REPORT] ?: true
    }

    suspend fun setMonthlyEmailReport(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MONTHLY_EMAIL_REPORT] = enabled
        }
    }

    val weeklySummary: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[WEEKLY_SUMMARY] ?: false
    }

    suspend fun setWeeklySummary(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[WEEKLY_SUMMARY] = enabled
        }
    }

    val currencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENCY_CODE] ?: "LKR"
    }

    suspend fun setCurrencyCode(currencyCode: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_CODE] = currencyCode.ifBlank { "LKR" }
        }
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[GEMINI_API_KEY].orEmpty()
    }

    suspend fun setGeminiApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[GEMINI_API_KEY] = apiKey.trim()
        }
    }

    val reminderHour: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_HOUR] ?: 20
    }

    val reminderMinute: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[REMINDER_MINUTE] ?: 0
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[REMINDER_HOUR] = hour.coerceIn(0, 23)
            prefs[REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }
}
