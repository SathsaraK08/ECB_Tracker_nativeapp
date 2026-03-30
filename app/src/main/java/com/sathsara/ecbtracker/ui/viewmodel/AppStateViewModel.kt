package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.logic.AppDestination
import com.sathsara.ecbtracker.logic.AppDestinationResolver
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppStateUiState(
    val isLoading: Boolean = true,
    val destination: AppDestination = AppDestination.LOGIN,
    val setupMessage: String? = null
)

@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val authRepository: AuthRepositoryContract,
    private val settingsRepository: SettingsRepositoryContract
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStateUiState())
    val uiState: StateFlow<AppStateUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val isConfigured = authRepository.isConfigured()
            val hasSession = if (isConfigured) {
                authRepository.hasActiveSession() || authRepository.isUserLoggedIn.first()
            } else {
                false
            }

            val profile = if (hasSession) settingsRepository.getProfile().getOrNull() else null
            val settings = if (hasSession) settingsRepository.getSettings().getOrNull() else null
            val destination = AppDestinationResolver.resolve(
                isConfigured = isConfigured,
                hasSession = hasSession,
                profile = profile,
                settings = settings
            )

            _uiState.value = AppStateUiState(
                isLoading = false,
                destination = destination,
                setupMessage = if (isConfigured) {
                    null
                } else {
                    "App setup is incomplete. Add SUPABASE_URL and SUPABASE_ANON_KEY before signing in."
                }
            )
        }
    }
}
