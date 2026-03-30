package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val supabase: SupabaseClient
) : SettingsRepositoryContract {
    private val settingsTable = supabase.postgrest["settings"]
    private val profilesTable = supabase.postgrest["profiles"]

    override suspend fun getSettings(): Result<UserSettings?> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@runCatching null
            
            settingsTable.select {
                filter { eq("user_id", userId) }
            }.decodeSingleOrNull<UserSettings>()
        }
    }

    override suspend fun updateSettings(settings: UserSettings): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            val finalSettings = settings.copy(userId = userId)
            
            settingsTable.upsert(finalSettings)
            Unit
        }
    }

    override suspend fun getProfile(): Result<Profile?> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@runCatching null
            
            profilesTable.select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<Profile>()
        }
    }
    
    override suspend fun updateProfile(profile: Profile): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            val finalProfile = profile.copy(id = userId)
            
            profilesTable.upsert(finalProfile)
            Unit
        }
    }
}
