package com.storymagic.kids.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.ui.screens.library.LibraryScreen
import com.storymagic.kids.ui.screens.loading.LoadingScreen
import com.storymagic.kids.ui.screens.onboarding.OnboardingScreen
import com.storymagic.kids.ui.screens.settings.SettingsScreen
import com.storymagic.kids.ui.screens.story.StoryScreen
import com.storymagic.kids.ui.theme.*

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Loading : Screen("loading")
    object Story : Screen("story/{storyId}") {
        fun createRoute(storyId: Int) = "story/$storyId"
    }
    object Settings : Screen("settings")
    object Library : Screen("library")
}

val bottomNavItems = listOf(
    BottomNavItem("Create", Icons.Default.Add, Screen.Onboarding.route),
    BottomNavItem("Library", Icons.AutoMirrored.Filled.LibraryBooks, Screen.Library.route),
    BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings.route)
)

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Onboarding.route) {
            LogManager.log("AppNavigation", "INFO", "OnboardingScreen composable loaded")
            OnboardingScreen(
                onNavigateToLoading = {
                    LogManager.log("AppNavigation", "INFO", "=== NAVIGATING TO LOADING SCREEN ===")
                    try {
                        navController.navigate(Screen.Loading.route)
                        LogManager.log("AppNavigation", "INFO", "Navigation to Loading successful")
                    } catch (e: Exception) {
                        LogManager.log("AppNavigation", "ERROR", "Navigation to Loading failed: ${e.message}")
                    }
                }
            )
        }
        
        composable(Screen.Loading.route) {
            LogManager.log("AppNavigation", "INFO", "=== LOADING SCREEN COMPOSABLE LOADED ===")
            LoadingScreen(
                onStoryGenerated = {
                    LogManager.log("AppNavigation", "INFO", "=== STORY GENERATED - NAVIGATING TO STORY SCREEN ===")
                    try {
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = false }
                        }
                        LogManager.log("AppNavigation", "INFO", "Navigation to Library successful")
                    } catch (e: Exception) {
                        LogManager.log("AppNavigation", "ERROR", "Navigation to Library failed: ${e.message}")
                        LogManager.log("AppNavigation", "ERROR", "Stack: ${android.util.Log.getStackTraceString(e)}")
                    }
                },
                onNavigateBack = {
                    LogManager.log("AppNavigation", "INFO", "Navigate back from Loading")
                    navController.popBackStack()
                },
                onNavigateToLibrary = {
                    LogManager.log("AppNavigation", "INFO", "Navigate to Library from Loading")
                    navController.navigate(Screen.Library.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = false }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Story.route,
            arguments = listOf(
                navArgument("storyId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getInt("storyId") ?: -1
            StoryScreen(
                storyId = if (storyId != -1) storyId else null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToNewStory = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Library.route) { inclusive = false }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToStory = { id ->
                    navController.navigate(Screen.Story.createRoute(id))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomNav = currentDestination?.route in listOf(
        Screen.Onboarding.route,
        Screen.Library.route,
        Screen.Settings.route
    )
    
    StoryMagicKidsTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomNav,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    BottomNavigationBar(
                        navController = navController,
                        currentDestination = currentDestination
                    )
                }
            }
        ) { _ ->
            AppNavHost(
                navController = navController,
                startDestination = Screen.Onboarding.route
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    NavigationBar(
        containerColor = SurfaceWhite
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.title, fontSize = 12.sp) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    if (item.route == Screen.Onboarding.route) {
                        navController.navigate(item.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryCoral,
                    selectedTextColor = PrimaryCoral,
                    indicatorColor = PrimaryCoral.copy(alpha = 0.1f)
                )
            )
        }
    }
}
