package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PostPropertyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedScreen
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ExploreViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PostPropertyViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.SavedViewModel

/**
 * Top-level destination navigation host for Ren.
 * Decoupled from MainActivity for production maintainability and testability.
 */
@Composable
fun QuickNestNavHost(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    exploreViewModel: ExploreViewModel,
    postViewModel: PostPropertyViewModel,
    savedViewModel: SavedViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = QuickNestRoute.Home,
        modifier = modifier.fillMaxSize()
    ) {
        composable<QuickNestRoute.Home> {
            HomeScreen(homeViewModel = homeViewModel, mainViewModel = mainViewModel)
        }
        composable<QuickNestRoute.Explore> {
            ExploreScreen(exploreViewModel = exploreViewModel, mainViewModel = mainViewModel)
        }
        composable<QuickNestRoute.PostProperty> {
            PostPropertyScreen(postViewModel = postViewModel)
        }
        composable<QuickNestRoute.Saved> {
            SavedScreen(savedViewModel = savedViewModel, mainViewModel = mainViewModel)
        }
        composable<QuickNestRoute.Profile> {
            val userRole by profileViewModel.userRole.collectAsStateWithLifecycle()
            val currentUserProfile by profileViewModel.currentUserProfile.collectAsStateWithLifecycle()
            ProfileScreen(
                userRole = userRole,
                currentUserProfile = currentUserProfile,
                onSetUserRole = { profileViewModel.setUserRole(it) },
                onFeedback = { mainViewModel.showFeedback(it) },
                onLogout = { authViewModel.logout() },
                onDeleteAccount = { profileViewModel.deleteAccount() }
            )
        }
    }
}
