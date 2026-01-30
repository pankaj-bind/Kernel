package com.example.kernel.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CompetitiveProgramming : Screen("competitive_programming")
    data object Alarmy : Screen("alarmy")
    data object Notes : Screen("notes")
}
