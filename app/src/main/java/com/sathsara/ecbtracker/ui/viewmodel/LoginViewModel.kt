package com.sathsara.ecbtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val isConfigured: Boolean = true
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepositoryContract
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(isConfigured = authRepository.isConfigured()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(email: String, pass: String) {
        if (!ensureConfigured()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, infoMessage = null)
            authRepository.signIn(email, pass)
                .onSuccess {
                    _uiState.value = LoginUiState(
                        isAuthenticated = authRepository.hasActiveSession(),
                        isConfigured = true
                    )
                }
                .onFailure {
                    _uiState.value = LoginUiState(
                        error = it.toFriendlyMessage("Sign in failed"),
                        isConfigured = true
                    )
                }
        }
    }

    fun signUp(email: String, pass: String) {
        if (!ensureConfigured()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, infoMessage = null)
            authRepository.signUp(email, pass)
                .onSuccess {
                    _uiState.value = if (authRepository.hasActiveSession()) {
                        LoginUiState(
                            isAuthenticated = true,
                            infoMessage = "Account created successfully.",
                            isConfigured = true
                        )
                    } else {
                        LoginUiState(
                            infoMessage = "Account created. Confirm your email, then sign in.",
                            isConfigured = true
                        )
                    }
                }
                .onFailure {
                    _uiState.value = LoginUiState(
                        error = it.toFriendlyMessage("Sign up failed"),
                        isConfigured = true
                    )
                }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(error = null, infoMessage = null)
    }

    fun sendPasswordReset(email: String) {
        if (!ensureConfigured()) return
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Enter your account email first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, infoMessage = null)
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.value = LoginUiState(
                        infoMessage = "Password reset email sent to $email",
                        isConfigured = true
                    )
                }
                .onFailure {
                    _uiState.value = LoginUiState(
                        error = it.toFriendlyMessage("Failed to send password reset email"),
                        isConfigured = true
                    )
                }
        }
    }

    private fun ensureConfigured(): Boolean {
        val configured = authRepository.isConfigured()
        if (!configured) {
            _uiState.value = LoginUiState(
                error = "Supabase configuration is missing or invalid. Add a valid https SUPABASE_URL and SUPABASE_ANON_KEY before continuing.",
                isConfigured = false
            )
        }
        return configured
    }
}


private fun Throwable.toFriendlyMessage(fallback: String): String {
    val message = this.message.orEmpty()
    return if (message.contains("Cannot resolve Supabase host")) {
        message
    } else {
        message.ifBlank { fallback }
    }
}
