package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations for QuickNest.
 */
sealed interface QuickNestRoute {

    @Serializable
    data object Onboarding : QuickNestRoute

    @Serializable
    data object Login : QuickNestRoute

    @Serializable
    data object Home : QuickNestRoute

    @Serializable
    data object Explore : QuickNestRoute

    @Serializable
    data object PostProperty : QuickNestRoute

    @Serializable
    data object Saved : QuickNestRoute

    @Serializable
    data object Profile : QuickNestRoute

    @Serializable
    data class PropertyDetail(val propertyId: String) : QuickNestRoute
}

/**
 * Bottom navigation items metadata.
 */
data class BottomNavItem(
    val route: QuickNestRoute,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

val topLevelNavItems = listOf(
    BottomNavItem(QuickNestRoute.Home, "Home", Icons.Default.Home, "nav_home"),
    BottomNavItem(QuickNestRoute.Explore, "Explore", Icons.Default.Explore, "nav_explore"),
    BottomNavItem(QuickNestRoute.PostProperty, "Post", Icons.Default.PostAdd, "nav_post"),
    BottomNavItem(QuickNestRoute.Saved, "Saved", Icons.Default.Favorite, "nav_saved"),
    BottomNavItem(QuickNestRoute.Profile, "Profile", Icons.Default.Person, "nav_profile")
)
