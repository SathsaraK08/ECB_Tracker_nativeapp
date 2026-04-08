package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.URI
import java.net.UnknownHostException
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
        if (BuildConfig.SUPABASE_ANON_KEY.isBlank()) return false
        val rawUrl = BuildConfig.SUPABASE_URL.trim()
        if (rawUrl.isBlank()) return false

        return runCatching {
            val uri = URI(rawUrl)
            uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
    }

    override suspend fun signIn(email: String, password: String) = Result.runCatching {
        verifySupabaseHost()
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> = Result.runCatching {
        verifySupabaseHost()
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }

    override suspend fun signOut() = Result.runCatching {
        supabase.auth.signOut()
    }

    override suspend fun sendPasswordReset(email: String) = Result.runCatching {
        verifySupabaseHost()
        supabase.auth.resetPasswordForEmail(email)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = Result.runCatching {
        supabase.auth.updateUser {
            password = newPassword
        }
        Unit
    }

    private suspend fun verifySupabaseHost() = withContext(Dispatchers.IO) {
        val host = runCatching { URI(BuildConfig.SUPABASE_URL.trim()).host }
            .getOrNull()
            .orEmpty()

        if (host.isBlank()) {
            throw IllegalStateException("Invalid SUPABASE_URL. Please set a full https URL in app config.")
        }

        try {
            InetAddress.getByName(host)
        } catch (_: UnknownHostException) {
            throw IllegalStateException(
                "Cannot resolve Supabase host '$host'. Check SUPABASE_URL for typos in your app or GitHub Action secrets."
            )
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
