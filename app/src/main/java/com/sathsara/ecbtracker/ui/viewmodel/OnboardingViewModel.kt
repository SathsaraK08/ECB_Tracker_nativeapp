package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val displayName: String = "",
    val accountNumber: String = "",
    val ratePerUnit: String = "32.0",
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepositoryContract
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadExistingValues()
    }

    fun updateDisplayName(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value, error = null)
    }

    fun updateAccountNumber(value: String) {
        _uiState.value = _uiState.value.copy(accountNumber = value, error = null)
    }

    fun updateRatePerUnit(value: String) {
        _uiState.value = _uiState.value.copy(ratePerUnit = value, error = null)
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val rate = state.ratePerUnit.toDoubleOrNull()

            if (state.displayName.isBlank() || state.accountNumber.isBlank() || rate == null || rate <= 0.0) {
                _uiState.value = state.copy(
                    error = "Enter your name, CEB account number, and a valid rate to continue."
                )
                return@launch
            }

            _uiState.value = state.copy(isSaving = true, error = null)

            val currentProfile = settingsRepository.getProfile().getOrNull() ?: Profile()
            val currentSettings = settingsRepository.getSettings().getOrNull() ?: UserSettings()
            val profileResult = settingsRepository.updateProfile(
                currentProfile.copy(username = state.displayName.trim())
            )
            val settingsResult = settingsRepository.updateSettings(
                currentSettings.copy(
                    lkrPerUnit = rate,
                    accountNumber = state.accountNumber.trim(),
                    ownerName = state.displayName.trim()
                )
            )

            val failure = profileResult.exceptionOrNull() ?: settingsResult.exceptionOrNull()
            _uiState.value = if (failure == null) {
                state.copy(isSaving = false, isComplete = true, error = null)
            } else {
                state.copy(isSaving = false, error = failure.message ?: "Failed to save setup")
            }
        }
    }

    private fun loadExistingValues() {
        viewModelScope.launch {
            val profile = settingsRepository.getProfile().getOrNull()
            val settings = settingsRepository.getSettings().getOrNull()

            _uiState.value = OnboardingUiState(
                isLoading = false,
                displayName = profile?.username.orEmpty(),
                accountNumber = settings?.accountNumber.orEmpty(),
                ratePerUnit = (settings?.lkrPerUnit?.takeIf { it > 0.0 } ?: 32.0).toString()
            )
        }
    }
}
