package com.sathsara.ecbtracker.ui.viewmodel

import com.sathsara.ecbtracker.MainDispatcherRule
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun signIn_sets_authenticated_state_on_successful_session() = runTest {
        val authRepository = FakeAuthRepository(
            signInResult = Result.success(Unit),
            hasActiveSession = true
        )
        val viewModel = LoginViewModel(authRepository)

        viewModel.signIn("user@example.com", "secret123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun signUp_shows_confirmation_message_when_session_is_not_created() = runTest {
        val authRepository = FakeAuthRepository(
            signUpResult = Result.success(Unit),
            hasActiveSession = false
        )
        val viewModel = LoginViewModel(authRepository)

        viewModel.signUp("user@example.com", "secret123")
        advanceUntilIdle()

        assertEquals(
            "Account created. Confirm your email, then sign in.",
            viewModel.uiState.value.infoMessage
        )
        assertTrue(!viewModel.uiState.value.isAuthenticated)
    }

    @Test
    fun signIn_reports_missing_configuration() {
        val authRepository = FakeAuthRepository(isConfigured = false)
        val viewModel = LoginViewModel(authRepository)

        viewModel.signIn("user@example.com", "secret123")

        assertTrue(viewModel.uiState.value.error!!.contains("Supabase configuration is missing"))
    }

    @Test
    fun sendPasswordReset_sets_info_message_on_success() = runTest {
        val authRepository = FakeAuthRepository(
            passwordResetResult = Result.success(Unit)
        )
        val viewModel = LoginViewModel(authRepository)

        viewModel.sendPasswordReset("user@example.com")
        advanceUntilIdle()

        assertEquals("Password reset email sent to user@example.com", viewModel.uiState.value.infoMessage)
    }
}

private class FakeAuthRepository(
    private val isConfigured: Boolean = true,
    private val signInResult: Result<Unit> = Result.failure(IllegalStateException("Unexpected sign-in")),
    private val signUpResult: Result<Unit> = Result.failure(IllegalStateException("Unexpected sign-up")),
    private val passwordResetResult: Result<Unit> = Result.success(Unit),
    private val hasActiveSession: Boolean = false
) : AuthRepositoryContract {
    override val isUserLoggedIn: Flow<Boolean> = MutableStateFlow(hasActiveSession)

    override fun isConfigured(): Boolean = isConfigured

    override suspend fun signIn(email: String, password: String): Result<Unit> = signInResult

    override suspend fun signUp(email: String, password: String): Result<Unit> = signUpResult

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)

    override suspend fun sendPasswordReset(email: String): Result<Unit> = passwordResetResult

    override suspend fun updatePassword(newPassword: String): Result<Unit> = Result.success(Unit)

    override fun getCurrentUserId(): String? = if (hasActiveSession) "user-id" else null

    override fun getCurrentUserEmail(): String? = if (hasActiveSession) "user@example.com" else null

    override fun hasActiveSession(): Boolean = hasActiveSession
}
