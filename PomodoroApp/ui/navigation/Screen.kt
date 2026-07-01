package com.musabber.pomofocus.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object AboutDeveloper : Screen("about_developer")
}