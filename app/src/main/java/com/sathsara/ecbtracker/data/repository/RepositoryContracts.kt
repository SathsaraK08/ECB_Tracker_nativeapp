package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AuthRepositoryContract {
    val isUserLoggedIn: Flow<Boolean>

    fun isConfigured(): Boolean

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(email: String, password: String): Result<Unit>

    suspend fun signOut(): Result<Unit>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    suspend fun updatePassword(newPassword: String): Result<Unit>

    fun getCurrentUserId(): String?

    fun getCurrentUserEmail(): String?

    fun hasActiveSession(): Boolean
}

interface SettingsRepositoryContract {
    suspend fun getSettings(): Result<UserSettings?>

    suspend fun updateSettings(settings: UserSettings): Result<Unit>

    suspend fun getProfile(): Result<Profile?>

    suspend fun updateProfile(profile: Profile): Result<Unit>
}

interface EntryRepositoryContract {
    suspend fun getEntries(limit: Long = 20, isVerified: Boolean? = null): Result<List<Entry>>

    suspend fun getRecentEntries(limit: Long = 3): Result<List<Entry>>

    suspend fun getEntriesForMonth(yearMonth: String): Result<List<Entry>>

    suspend fun getEntriesForRange(startDate: String, endDate: String): Result<List<Entry>>

    suspend fun insertEntry(entry: Entry, photoFile: File? = null): Result<Unit>

    suspend fun deleteEntry(id: String): Result<Unit>

    suspend fun getLatestEntryBefore(date: String, time: String): Result<Entry?>
}
