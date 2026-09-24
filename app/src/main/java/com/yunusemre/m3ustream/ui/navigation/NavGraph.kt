package com.yunusemre.m3ustream.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yunusemre.m3ustream.R
import com.yunusemre.m3ustream.ui.detail.MovieDetailScreen
import com.yunusemre.m3ustream.ui.detail.SeriesDetailScreen
import com.yunusemre.m3ustream.ui.home.HomeScreen
import com.yunusemre.m3ustream.ui.player.PlayerScreen
import com.yunusemre.m3ustream.ui.search.SearchScreen
import com.yunusemre.m3ustream.ui.settings.SettingsScreen
import com.yunusemre.m3ustream.ui.favorites.FavoritesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isBottomNavVisible = currentDestination?.route in listOf(
                Screen.Home.route,
                Screen.Search.route,
                Screen.Favorites.route,
                Screen.Settings.route
            )

            if (isBottomNavVisible) {
                NavigationBar {
                    val items = listOf(
                        Screen.Home to Pair(R.string.home, Icons.Filled.Home),
                        Screen.Search to Pair(R.string.search, Icons.Filled.Search),
                        Screen.Favorites to Pair(R.string.favorites, Icons.Filled.Favorite),
                        Screen.Settings to Pair(R.string.settings, Icons.Filled.Settings)
                    )

                    items.forEach { (screen, info) ->
                        NavigationBarItem(
                            icon = { Icon(info.second, contentDescription = stringResource(info.first)) },
                            label = { Text(stringResource(info.first)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                if (screen.route == Screen.Home.route) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Search.route) {
                SearchScreen(navController = navController)
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable(
                route = Screen.MovieDetail.route,
                arguments = listOf(navArgument("contentId") { type = NavType.StringType }),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
            ) {
                MovieDetailScreen(navController = navController)
            }
            composable(
                route = Screen.SeriesDetail.route,
                arguments = listOf(navArgument("seriesName") { type = NavType.StringType }),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
            ) {
                SeriesDetailScreen(navController = navController)
            }
            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("contentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
                PlayerScreen(
                    contentId = contentId,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
        }
    }
}
