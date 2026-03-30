package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) : AuthRepositoryContract {
    override val isUserLoggedIn: Flow<Boolean> = supabase.auth.sessionStatus.map {
        it is io.github.jan.supabase.auth.status.SessionStatus.Authenticated
    }

    override fun isConfigured(): Boolean {
        return BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()
    }

    override suspend fun signIn(email: String, password: String) = Result.runCatching {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String) = Result.runCatching {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() = Result.runCatching {
        supabase.auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String) = Result.runCatching {
        supabase.auth.resetPasswordForEmail(email)
    }

    override suspend fun updatePassword(newPassword: String) = Result.runCatching {
        supabase.auth.updateUser {
            password = newPassword
        }
    }

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    override fun getCurrentUserEmail(): String? {
        return supabase.auth.currentUserOrNull()?.email
    }

    override fun hasActiveSession(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
}
