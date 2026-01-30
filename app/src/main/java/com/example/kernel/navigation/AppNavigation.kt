package com.example.kernel.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kernel.ui.cp.CompetitiveProgrammingScreen
import com.example.kernel.ui.home.HomeScreen

/**
 * Navigation graph for the app
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.CompetitiveProgramming.route) {
            CompetitiveProgrammingScreen()
        }

        composable(Screen.Alarmy.route) {
            PlaceholderScreen(title = "Alarmy Clone")
        }

        composable(Screen.Notes.route) {
            PlaceholderScreen(title = "Notes")
        }
    }
}

/**
 * Placeholder screen for features not yet implemented
 */
@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "$title - Coming Soon!",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}
