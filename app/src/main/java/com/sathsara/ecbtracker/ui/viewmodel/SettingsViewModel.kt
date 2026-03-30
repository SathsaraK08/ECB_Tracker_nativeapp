package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import com.sathsara.ecbtracker.logic.TrackerDateTimeParser
import com.sathsara.ecbtracker.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val settings: UserSettings? = null,
    val email: String = "",
    val currencyCode: String = "LKR",
    val geminiApiKey: String = "",
    val reminderTime: String = "20:00",
    val error: String? = null,
    val saveMessage: String? = null,
    val isSignedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepositoryContract,
    private val authRepository: AuthRepositoryContract,
    private val dataStoreManager: DataStoreManager,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // DataStore states mapped as StateFlows
    val isDarkMode = dataStoreManager.isDarkMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val billReminders = dataStoreManager.billReminders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val usageAlerts = dataStoreManager.usageAlerts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val weeklyDigest = dataStoreManager.weeklyDigest.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val previousState = _uiState.value
            _uiState.value = previousState.copy(isLoading = true)
            
            val profile = settingsRepository.getProfile().getOrNull()
            val settings = settingsRepository.getSettings().getOrNull()
            val currentEmail = authRepository.getCurrentUserEmail().orEmpty()
            val currencyCode = dataStoreManager.currencyCode.first()
            val geminiApiKey = dataStoreManager.geminiApiKey.first()
            val reminderTime = TrackerDateTimeParser.formatTime(
                dataStoreManager.reminderHour.first(),
                dataStoreManager.reminderMinute.first()
            )
            
            _uiState.value = SettingsUiState(
                isLoading = false,
                profile = profile,
                settings = settings,
                email = currentEmail,
                currencyCode = currencyCode,
                geminiApiKey = geminiApiKey,
                reminderTime = reminderTime,
                saveMessage = previousState.saveMessage
            )
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setDarkMode(enabled) }
    }

    fun toggleBillReminders(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setBillReminders(enabled)
            if (enabled) {
                reminderScheduler.scheduleDaily(
                    hour = dataStoreManager.reminderHour.first(),
                    minute = dataStoreManager.reminderMinute.first()
                )
            } else {
                reminderScheduler.cancelDaily()
            }
        }
    }

    fun toggleUsageAlerts(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setUsageAlerts(enabled) }
    }

    fun toggleWeeklyDigest(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.setWeeklyDigest(enabled) }
    }

    fun saveAccountSettings(
        displayName: String,
        accountNumber: String,
        rateText: String,
        currencyCode: String,
        geminiApiKey: String,
        reminderTime: String
    ) {
        viewModelScope.launch {
            val rate = rateText.toDoubleOrNull()
            val reminder = TrackerDateTimeParser.parseReminderTime(reminderTime)
            if (
                displayName.isBlank() ||
                accountNumber.isBlank() ||
                rate == null ||
                rate <= 0.0 ||
                reminder == null
            ) {
                _uiState.value = _uiState.value.copy(
                    error = "Enter a valid name, account number, rate, and reminder time."
                )
                return@launch
            }

            val currentProfile = _uiState.value.profile ?: Profile()
            val currentSettings = _uiState.value.settings ?: UserSettings()
            val profileResult = settingsRepository.updateProfile(
                currentProfile.copy(username = displayName.trim())
            )
            val settingsResult = settingsRepository.updateSettings(
                currentSettings.copy(
                    accountNumber = accountNumber.trim(),
                    ownerName = displayName.trim(),
                    lkrPerUnit = rate
                )
            )

            val error = profileResult.exceptionOrNull() ?: settingsResult.exceptionOrNull()
            if (error != null) {
                _uiState.value = _uiState.value.copy(error = error.message ?: "Failed to save changes")
            } else {
                dataStoreManager.setCurrencyCode(currencyCode.trim().uppercase())
                dataStoreManager.setGeminiApiKey(geminiApiKey)
                dataStoreManager.setReminderTime(reminder.first, reminder.second)
                if (billReminders.first()) {
                    reminderScheduler.scheduleDaily(reminder.first, reminder.second)
                }
                _uiState.value = _uiState.value.copy(error = null, saveMessage = "Settings updated")
                loadData()
            }
        }
    }

    fun sendPasswordReset() {
        viewModelScope.launch {
            val email = _uiState.value.email
            if (email.isBlank()) {
                _uiState.value = _uiState.value.copy(error = "Add an email address before requesting a password reset.")
                return@launch
            }

            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        error = null,
                        saveMessage = "Password reset email sent to $email"
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "Failed to send password reset email."
                    )
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = _uiState.value.copy(isSignedOut = true)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, saveMessage = null)
    }
}
