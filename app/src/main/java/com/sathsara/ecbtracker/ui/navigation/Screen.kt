package com.sathsara.ecbtracker.ui.navigation

sealed class Screen(val route: String) {
    object Gate : Screen("gate")
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Home : Screen("home")
    object Log : Screen("log")
    object History : Screen("history")
    object Settings : Screen("settings")
}
