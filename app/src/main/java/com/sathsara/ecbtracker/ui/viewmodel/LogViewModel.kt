package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.repository.EntryRepositoryContract
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import com.sathsara.ecbtracker.logic.MeterReadingParser
import com.sathsara.ecbtracker.logic.TrackerDateTimeParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.io.File
import javax.inject.Inject

data class LogUiState(
    val isLoading: Boolean = false,
    val previousUnit: Double = 0.0,
    val dateInput: String = LocalDateTime.now().toLocalDate().toString(),
    val timeInput: String = TrackerDateTimeParser.formatTime(LocalDateTime.now().hour, LocalDateTime.now().minute),
    val currentUnitInput: String = "",
    val usagePreview: Double? = null,
    val ratePerUnit: Double = 32.0,
    val currencyCode: String = "LKR",
    val selectedAppliances: Set<String> = emptySet(),
    val note: String = "",
    val photoFile: File? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val entryRepository: EntryRepositoryContract,
    private val settingsRepository: SettingsRepositoryContract,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogUiState())
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    init {
        loadTrackerPreferences()
        refreshReadingContext()
    }

    private fun loadTrackerPreferences() {
        viewModelScope.launch {
            val ratePerUnit = settingsRepository.getSettings().getOrNull()?.lkrPerUnit ?: 32.0
            val currencyCode = dataStoreManager.currencyCode.first()
            _uiState.value = _uiState.value.copy(
                ratePerUnit = ratePerUnit,
                currencyCode = currencyCode
            )
        }
    }

    private fun refreshReadingContext() {
        val state = _uiState.value
        val date = TrackerDateTimeParser.parseDate(state.dateInput)
        val time = TrackerDateTimeParser.parseTime(state.timeInput)

        if (date == null || time == null) {
            _uiState.value = syncUsagePreview(state.copy(previousUnit = 0.0))
            return
        }

        viewModelScope.launch {
            val previousEntry = entryRepository.getLatestEntryBefore(
                date = date.toString(),
                time = TrackerDateTimeParser.formatTime(time.hour, time.minute)
            ).getOrNull()

            _uiState.value = syncUsagePreview(
                _uiState.value.copy(previousUnit = previousEntry?.unit ?: 0.0)
            )
        }
    }

    private fun syncUsagePreview(state: LogUiState): LogUiState {
        val currentReading = MeterReadingParser.parse(state.currentUnitInput)
        val usagePreview = currentReading?.let { (it - state.previousUnit).coerceAtLeast(0.0) }
        return state.copy(usagePreview = usagePreview)
    }

    fun updateUnitInput(input: String) {
        _uiState.value = syncUsagePreview(
            _uiState.value.copy(
                currentUnitInput = MeterReadingParser.sanitize(input),
                error = null
            )
        )
    }

    fun updateDateInput(input: String) {
        _uiState.value = _uiState.value.copy(
            dateInput = TrackerDateTimeParser.sanitizeDate(input),
            error = null
        )
        refreshReadingContext()
    }

    fun updateTimeInput(input: String) {
        _uiState.value = _uiState.value.copy(
            timeInput = TrackerDateTimeParser.sanitizeTime(input),
            error = null
        )
        refreshReadingContext()
    }

    fun toggleAppliance(appliance: String) {
        val current = _uiState.value.selectedAppliances.toMutableSet()
        if (current.contains(appliance)) {
            current.remove(appliance)
        } else {
            current.add(appliance)
        }
        _uiState.value = _uiState.value.copy(selectedAppliances = current)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note, error = null)
    }

    fun setPhoto(file: File) {
        _uiState.value = _uiState.value.copy(photoFile = file, error = null)
    }

    fun submitReading() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val state = _uiState.value
            val parsedDate = TrackerDateTimeParser.parseDate(state.dateInput)
            val parsedTime = TrackerDateTimeParser.parseTime(state.timeInput)

            if (parsedDate == null || parsedTime == null) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Enter a valid date and time for the reading."
                )
                return@launch
            }

            val validation = MeterReadingParser.validate(state.currentUnitInput, state.previousUnit)
            val parsedDouble = validation.reading

            if (parsedDouble == null) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = validation.errorMessage
                )
                return@launch
            }

            val usedAmount = (parsedDouble - state.previousUnit).coerceAtLeast(0.0)
            val timeStr = TrackerDateTimeParser.formatTime(parsedTime.hour, parsedTime.minute)

            val entry = Entry(
                date = parsedDate.toString(),
                time = timeStr,
                unit = parsedDouble,
                used = usedAmount,
                note = state.note.takeIf { it.isNotBlank() },
                appliances = state.selectedAppliances.toList()
            )

            entryRepository.insertEntry(entry, state.photoFile)
                .onSuccess {
                    _uiState.value = state.copy(isLoading = false, isSuccess = true)
                }
                .onFailure {
                    _uiState.value = state.copy(isLoading = false, error = it.message ?: "Failed to save reading")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
