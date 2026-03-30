package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.ForecastTip
import com.sathsara.ecbtracker.data.repository.EntryRepositoryContract
import com.sathsara.ecbtracker.data.repository.ForecastRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import com.sathsara.ecbtracker.logic.DashboardMetricsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val username: String = "User",
    val accountNumber: String = "",
    val monthlyKwh: Double = 0.0,
    val todayKwh: Double = 0.0,
    val weeklyKwh: Double = 0.0,
    val averageDailyKwh: Double = 0.0,
    val currentBill: Double = 0.0,
    val projectedBill: Double = 0.0,
    val ratePerUnit: Double = 32.0,
    val currencyCode: String = "LKR",
    val peakHours: String = "",
    val forecastTips: List<ForecastTip> = emptyList(),
    val chartData: List<Pair<String, Float>> = emptyList(),
    val recentActivity: List<Entry> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val entryRepository: EntryRepositoryContract,
    private val settingsRepository: SettingsRepositoryContract,
    private val dataStoreManager: DataStoreManager,
    private val forecastRepository: ForecastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Get current user profile and settings
            val profile = settingsRepository.getProfile().getOrNull()
            val settings = settingsRepository.getSettings().getOrNull()
            val currencyCode = dataStoreManager.currencyCode.first()

            val currentMoment = LocalDateTime.now()
            val monthStr = currentMoment.monthValue.toString().padStart(2, '0')
            val yearMonth = "${currentMoment.year}-$monthStr"

            val monthEntries = entryRepository.getEntriesForMonth(yearMonth).getOrNull() ?: emptyList()
            val recentEntries = entryRepository.getRecentEntries(3).getOrNull() ?: emptyList()
            val metrics = DashboardMetricsCalculator.build(
                username = profile?.username,
                settings = settings,
                monthEntries = monthEntries,
                today = currentMoment.toLocalDate()
            )
            val forecast = forecastRepository.getMonthlyForecast(
                yearMonth = yearMonth,
                today = currentMoment.toLocalDate()
            ).getOrNull()

            _uiState.value = HomeUiState(
                isLoading = false,
                username = metrics.username,
                accountNumber = metrics.accountNumber,
                monthlyKwh = metrics.monthlyKwh,
                todayKwh = metrics.todayKwh,
                weeklyKwh = metrics.weeklyKwh,
                averageDailyKwh = metrics.averageDailyKwh,
                currentBill = metrics.currentBill,
                projectedBill = forecast?.projectedBill ?: metrics.projectedBill,
                ratePerUnit = metrics.ratePerUnit,
                currencyCode = currencyCode,
                peakHours = forecast?.peakHours.orEmpty(),
                forecastTips = forecast?.tips.orEmpty(),
                chartData = metrics.chartData,
                recentActivity = recentEntries
            )
        }
    }
}
