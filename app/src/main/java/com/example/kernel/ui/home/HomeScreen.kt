package com.example.kernel.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kernel.navigation.Screen

/**
 * Home screen with bottom navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val items = listOf(
        BottomNavItem(
            title = "CP Contests",
            icon = Icons.Default.EmojiEvents,
            screen = Screen.CompetitiveProgramming
        ),
        BottomNavItem(
            title = "Alarmy",
            icon = Icons.Default.Alarm,
            screen = Screen.Alarmy
        ),
        BottomNavItem(
            title = "Notes",
            icon = Icons.Default.Note,
            screen = Screen.Notes
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Content will be displayed through navigation
        Box(modifier = Modifier.padding(paddingValues)) {
            // The actual content is controlled by the navigation
        }
    }
}

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val screen: Screen
)
