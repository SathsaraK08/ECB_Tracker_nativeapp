package com.sathsara.ecbtracker.ui.viewmodel

import com.sathsara.ecbtracker.MainDispatcherRule
import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import com.sathsara.ecbtracker.logic.AppDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refresh_routes_to_login_when_backend_is_not_configured() = runTest {
        val viewModel = AppStateViewModel(
            authRepository = FakeGateAuthRepository(isConfigured = false, hasSession = false),
            settingsRepository = FakeSettingsRepository()
        )

        advanceUntilIdle()

        assertEquals(AppDestination.LOGIN, viewModel.uiState.value.destination)
        assertEquals(
            "App setup is incomplete. Add SUPABASE_URL and SUPABASE_ANON_KEY before signing in.",
            viewModel.uiState.value.setupMessage
        )
    }

    @Test
    fun refresh_routes_to_onboarding_when_profile_is_incomplete() = runTest {
        val viewModel = AppStateViewModel(
            authRepository = FakeGateAuthRepository(isConfigured = true, hasSession = true),
            settingsRepository = FakeSettingsRepository(
                profile = Profile(username = ""),
                settings = UserSettings(accountNumber = "123", lkrPerUnit = 32.0)
            )
        )

        advanceUntilIdle()

        assertEquals(AppDestination.ONBOARDING, viewModel.uiState.value.destination)
    }

    @Test
    fun refresh_routes_to_main_when_session_and_setup_are_ready() = runTest {
        val viewModel = AppStateViewModel(
            authRepository = FakeGateAuthRepository(isConfigured = true, hasSession = true),
            settingsRepository = FakeSettingsRepository(
                profile = Profile(username = "Sathsara"),
                settings = UserSettings(accountNumber = "123", lkrPerUnit = 32.0)
            )
        )

        advanceUntilIdle()

        assertEquals(AppDestination.MAIN, viewModel.uiState.value.destination)
    }
}

private class FakeGateAuthRepository(
    private val isConfigured: Boolean,
    private val hasSession: Boolean
) : AuthRepositoryContract {
    override val isUserLoggedIn: Flow<Boolean> = MutableStateFlow(hasSession)

    override fun isConfigured(): Boolean = isConfigured

    override suspend fun signIn(email: String, password: String): Result<Unit> = Result.success(Unit)

    override suspend fun signUp(email: String, password: String): Result<Unit> = Result.success(Unit)

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun sendPasswordReset(email: String): Result<Unit> = Result.success(Unit)

    override suspend fun updatePassword(newPassword: String): Result<Unit> = Result.success(Unit)

    override fun getCurrentUserId(): String? = if (hasSession) "user-id" else null

    override fun getCurrentUserEmail(): String? = if (hasSession) "user@example.com" else null

    override fun hasActiveSession(): Boolean = hasSession
}

private class FakeSettingsRepository(
    private val profile: Profile? = null,
    private val settings: UserSettings? = null
) : SettingsRepositoryContract {
    override suspend fun getSettings(): Result<UserSettings?> = Result.success(settings)

    override suspend fun updateSettings(settings: UserSettings): Result<Unit> = Result.success(Unit)

    override suspend fun getProfile(): Result<Profile?> = Result.success(profile)

    override suspend fun updateProfile(profile: Profile): Result<Unit> = Result.success(Unit)
}
