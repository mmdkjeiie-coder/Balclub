package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Minutes : Screen("minutes", "Minutes", Icons.Filled.Assignment)
    object Events : Screen("events", "Events", Icons.Filled.Event)
    object Members : Screen("members", "Members", Icons.Filled.People)
    object More : Screen("more", "More", Icons.Filled.Menu)
}
