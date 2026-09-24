package com.yunusemre.m3ustream.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    
    object MovieDetail : Screen("movie_detail/{contentId}") {
        fun createRoute(contentId: String) = "movie_detail/$contentId"
    }
    
    object SeriesDetail : Screen("series_detail/{seriesName}") {
        fun createRoute(seriesName: String) = "series_detail/$seriesName"
    }
    
    object Player : Screen("player/{contentId}") {
        fun createRoute(contentId: String) = "player/$contentId"
    }
}
