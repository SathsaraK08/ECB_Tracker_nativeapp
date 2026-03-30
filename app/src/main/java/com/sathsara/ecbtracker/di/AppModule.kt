package com.sathsara.ecbtracker.di

import android.content.Context
import com.sathsara.ecbtracker.data.DataStoreManager
import com.sathsara.ecbtracker.data.SupabaseClientProvider
import com.sathsara.ecbtracker.data.repository.AuthRepository
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import com.sathsara.ecbtracker.data.repository.EntryRepository
import com.sathsara.ecbtracker.data.repository.EntryRepositoryContract
import com.sathsara.ecbtracker.data.repository.PaymentRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import com.sathsara.ecbtracker.data.service.ExportManager
import com.sathsara.ecbtracker.data.service.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClientProvider.client
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(client: SupabaseClient): AuthRepository {
        return AuthRepository(client)
    }

    @Provides
    @Singleton
    fun provideAuthRepositoryContract(repository: AuthRepository): AuthRepositoryContract {
        return repository
    }

    @Provides
    @Singleton
    fun provideEntryRepository(client: SupabaseClient): EntryRepository {
        return EntryRepository(client)
    }

    @Provides
    @Singleton
    fun provideEntryRepositoryContract(repository: EntryRepository): EntryRepositoryContract {
        return repository
    }

    @Provides
    @Singleton
    fun providePaymentRepository(client: SupabaseClient): PaymentRepository {
        return PaymentRepository(client)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(client: SupabaseClient): SettingsRepository {
        return SettingsRepository(client)
    }

    @Provides
    @Singleton
    fun provideSettingsRepositoryContract(repository: SettingsRepository): SettingsRepositoryContract {
        return repository
    }

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService()
    }

    @Provides
    @Singleton
    fun provideExportManager(@ApplicationContext context: Context): ExportManager {
        return ExportManager(context)
    }
}
