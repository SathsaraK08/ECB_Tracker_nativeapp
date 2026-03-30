package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.repository.EntryRepositoryContract
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class RecordsUiState(
    val isLoading: Boolean = true,
    val entries: List<Entry> = emptyList(),
    val ratePerUnit: Double = 32.0,
    val currencyCode: String = "LKR",
    val todayKwh: Double = 0.0,
    val weeklyKwh: Double = 0.0,
    val monthlyKwh: Double = 0.0,
    val filterMode: FilterMode = FilterMode.ALL,
    val error: String? = null
)

enum class FilterMode { ALL, VERIFIED, PENDING }

@HiltViewModel
class RecordsViewModel @Inject constructor(
    private val entryRepository: EntryRepositoryContract,
    private val settingsRepository: SettingsRepositoryContract,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val rateResult = settingsRepository.getSettings()
            val rate = rateResult.getOrNull()?.lkrPerUnit ?: 32.0
            val currencyCode = dataStoreManager.currencyCode.first()

            entryRepository.getEntries(limit = 100, isVerified = null)
                .onSuccess { allEntries ->
                    val today = LocalDate.now()
                    val weeklyStart = today.minusDays(6)
                    val filteredEntries = when (_uiState.value.filterMode) {
                        FilterMode.ALL -> allEntries
                        FilterMode.VERIFIED -> allEntries.filter { !it.imgUrl.isNullOrBlank() }
                        FilterMode.PENDING -> allEntries.filter { it.imgUrl.isNullOrBlank() }
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entries = filteredEntries,
                        ratePerUnit = rate,
                        currencyCode = currencyCode,
                        todayKwh = allEntries.filter { it.date == today.toString() }.sumOf { it.used },
                        weeklyKwh = allEntries.filter {
                            runCatching { LocalDate.parse(it.date) }.getOrNull()?.let { entryDate ->
                                !entryDate.isBefore(weeklyStart) && !entryDate.isAfter(today)
                            } ?: false
                        }.sumOf { it.used },
                        monthlyKwh = allEntries.sumOf { it.used }
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to load records"
                    )
                }
        }
    }

    fun setFilterMode(mode: FilterMode) {
        _uiState.value = _uiState.value.copy(filterMode = mode)
        loadData()
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            entryRepository.deleteEntry(id)
                .onSuccess { loadData() }
                .onFailure {
                    _uiState.value = _uiState.value.copy(error = it.message ?: "Failed to delete")
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
