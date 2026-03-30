package com.sathsara.ecbtracker.logic

import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AppDestinationResolverTest {

    @Test
    fun resolve_returns_login_when_configuration_missing() {
        val destination = AppDestinationResolver.resolve(
            isConfigured = false,
            hasSession = true,
            profile = Profile(username = "User"),
            settings = UserSettings(accountNumber = "123", lkrPerUnit = 35.0)
        )

        assertEquals(AppDestination.LOGIN, destination)
    }

    @Test
    fun resolve_returns_onboarding_when_profile_or_settings_are_incomplete() {
        val destination = AppDestinationResolver.resolve(
            isConfigured = true,
            hasSession = true,
            profile = Profile(username = ""),
            settings = UserSettings(accountNumber = "", lkrPerUnit = 0.0)
        )

        assertEquals(AppDestination.ONBOARDING, destination)
    }

    @Test
    fun resolve_returns_main_when_session_and_setup_are_ready() {
        val destination = AppDestinationResolver.resolve(
            isConfigured = true,
            hasSession = true,
            profile = Profile(username = "Sathsara"),
            settings = UserSettings(accountNumber = "123", lkrPerUnit = 32.0)
        )

        assertEquals(AppDestination.MAIN, destination)
    }
}
