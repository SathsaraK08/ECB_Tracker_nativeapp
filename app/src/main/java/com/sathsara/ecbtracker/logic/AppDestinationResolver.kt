package com.sathsara.ecbtracker.logic

import com.sathsara.ecbtracker.data.model.Profile
import com.sathsara.ecbtracker.data.model.UserSettings

enum class AppDestination {
    LOGIN,
    ONBOARDING,
    MAIN
}

object AppDestinationResolver {
    fun resolve(
        isConfigured: Boolean,
        hasSession: Boolean,
        profile: Profile?,
        settings: UserSettings?
    ): AppDestination {
        if (!isConfigured || !hasSession) {
            return AppDestination.LOGIN
        }

        return if (needsOnboarding(profile, settings)) {
            AppDestination.ONBOARDING
        } else {
            AppDestination.MAIN
        }
    }

    fun needsOnboarding(profile: Profile?, settings: UserSettings?): Boolean {
        val hasName = !profile?.username.isNullOrBlank()
        val hasAccount = !settings?.accountNumber.isNullOrBlank()
        val hasRate = (settings?.lkrPerUnit ?: 0.0) > 0.0
        return !(hasName && hasAccount && hasRate)
    }
}
