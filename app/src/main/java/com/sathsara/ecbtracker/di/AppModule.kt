package com.sathsara.ecbtracker.di

import com.sathsara.ecbtracker.data.SupabaseClientProvider
import com.sathsara.ecbtracker.data.repository.AuthRepository
import com.sathsara.ecbtracker.data.repository.AuthRepositoryContract
import com.sathsara.ecbtracker.data.repository.EntryRepository
import com.sathsara.ecbtracker.data.repository.EntryRepositoryContract
import com.sathsara.ecbtracker.data.repository.SettingsRepository
import com.sathsara.ecbtracker.data.repository.SettingsRepositoryContract
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: AuthRepository): AuthRepositoryContract

    @Binds
    @Singleton
    abstract fun bindEntryRepository(repository: EntryRepository): EntryRepositoryContract

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(repository: SettingsRepository): SettingsRepositoryContract
}
