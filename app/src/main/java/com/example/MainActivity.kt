package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.BlueCorporate
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SlateMutedText
import com.example.ui.theme.SlateSecondaryText
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.CharcoalNavyText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.BudgetFilter
import com.example.data.model.Property
import com.example.data.model.SortOption
import com.example.ui.navigation.QuickNestRoute
import com.example.ui.navigation.topLevelNavItems
import com.example.ui.components.AiAssistantDialog
import com.example.ui.components.ChatDialog
import com.example.ui.components.ContactSellerDialog
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.PropertyDetailSheet
import com.example.ui.components.QuickMatchDialog
import com.example.ui.components.ReportDialog
import com.example.ui.components.SmartMatchDialog
import com.example.ui.components.VisitBookingDialog
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PostPropertyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CoolGlassmorphicBorder
import com.example.ui.theme.CoolHeroGradient
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UrgencyFlame
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ExploreViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PostPropertyViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.QuickNestViewModel
import com.example.viewmodel.SavedViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false) {
                QuickNestApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNestApp(
    mainViewModel: MainViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel(),
    homeViewModel: HomeViewModel = koinViewModel(),
    exploreViewModel: ExploreViewModel = koinViewModel(),
    postViewModel: PostPropertyViewModel = koinViewModel(),
    savedViewModel: SavedViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    legacyViewModel: QuickNestViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isOnboarded by authViewModel.isOnboarded.collectAsStateWithLifecycle()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()

    val currentTab by mainViewModel.currentTab.collectAsStateWithLifecycle()
    val selectedProperty by mainViewModel.selectedProperty.collectAsStateWithLifecycle()
    val contactSellerProperty by mainViewModel.contactSellerProperty.collectAsStateWithLifecycle()
    val chatProperty by mainViewModel.chatProperty.collectAsStateWithLifecycle()
    val visitProperty by mainViewModel.visitProperty.collectAsStateWithLifecycle()
    val quickMatchProperty by mainViewModel.quickMatchProperty.collectAsStateWithLifecycle()
    val showAiAssistant by mainViewModel.showAiAssistant.collectAsStateWithLifecycle()
    val reportProperty by mainViewModel.reportProperty.collectAsStateWithLifecycle()
    val showFilterSheet by mainViewModel.showFilterSheet.collectAsStateWithLifecycle()
    val feedbackMessage by mainViewModel.feedbackMessage.collectAsStateWithLifecycle()

    val selectedCategory by homeViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPropertyType by homeViewModel.selectedPropertyType.collectAsStateWithLifecycle()
    val selectedBudget by homeViewModel.selectedBudget.collectAsStateWithLifecycle()
    val selectedBedrooms by homeViewModel.selectedBedrooms.collectAsStateWithLifecycle()
    val selectedSortOption by homeViewModel.selectedSortOption.collectAsStateWithLifecycle()
    val verifiedOnly by homeViewModel.verifiedOnly.collectAsStateWithLifecycle()
    val urgentOnly by homeViewModel.urgentOnly.collectAsStateWithLifecycle()

    val chatMessages by mainViewModel.chatMessages.collectAsStateWithLifecycle()
    val aiQuery by mainViewModel.aiQuery.collectAsStateWithLifecycle()
    val aiResults by mainViewModel.aiResults.collectAsStateWithLifecycle()
    val aiExplanation by mainViewModel.aiExplanation.collectAsStateWithLifecycle()
    val showSmartMatchDialog by mainViewModel.showSmartMatchDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            mainViewModel.clearFeedbackMessage()
        }
    }

    if (!isOnboarded) {
        com.example.ui.screens.OnboardingScreen(
            onFinishOnboarding = { authViewModel.completeOnboarding() }
        )
        return
    }

    if (!isLoggedIn) {
        com.example.ui.screens.LoginScreen(
            onGoogleSignIn = { idToken, city, onError ->
                authViewModel.signInWithGoogle(idToken, city) { result ->
                    result.onFailure { onError(it.message ?: "Google Sign-In failed") }
                }
            },
            onContinueAsGuest = {
                authViewModel.continueAsGuest()
            },
            onEmailSignIn = { email, password, onError ->
                authViewModel.signInWithEmail(email, password) { result ->
                    result.onFailure { onError(it.message ?: "Sign in failed") }
                }
            },
            onEmailSignUp = { name, email, password, role, city, onError ->
                authViewModel.registerWithEmail(email, password, name, role, city) { result ->
                    result.onFailure { onError(it.message ?: "Registration failed") }
                }
            }
        )
        return
    }

    BackHandler(enabled = currentTab != 0) {
        mainViewModel.setTab(0)
    }

    LaunchedEffect(currentTab) {
        val targetRoute: QuickNestRoute = when (currentTab) {
            0 -> QuickNestRoute.Home
            1 -> QuickNestRoute.Explore
            2 -> QuickNestRoute.PostProperty
            3 -> QuickNestRoute.Saved
            4 -> QuickNestRoute.Profile
            else -> QuickNestRoute.Home
        }
        val graph = runCatching { navController.graph }.getOrNull()
        if (graph != null && currentDestination?.hasRoute(targetRoute::class) != true) {
            navController.navigate(targetRoute) {
                popUpTo(graph.findStartDestination().id) {
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = IvoryBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFAF8F5))
                                .border(1.dp, CardBorderSubtle, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ren_logo),
                                contentDescription = "Ren Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Ren",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFAF5EB),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                                    modifier = Modifier.scale(pulseScale)
                                ) {
                                    Text(
                                        text = "INDIA",
                                        color = AccentGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Find Your Place • Real Estate India 🇮🇳",
                                fontSize = 10.sp,
                                color = SlateSecondaryText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { mainViewModel.openSmartMatchDialog() },
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("top_bar_smart_match_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Smart match",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    IconButton(
                        onClick = { mainViewModel.openAiAssistant() },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Property Matcher",
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IvoryBackground,
                    titleContentColor = CharcoalNavyText
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0xFF244466), RoundedCornerShape(28.dp))
                    .testTag("bottom_navigation_bar"),
                color = NavyPrimary,
                shape = RoundedCornerShape(28.dp)
            ) {
                NavigationBar(
                    containerColor = NavyPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    topLevelNavItems.forEachIndexed { index, item ->
                        val isSelected = currentTab == index
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "nav_scale_$index"
                        )

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentTab != index) {
                                    mainViewModel.setTab(index)
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .scale(iconScale)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = BlueCorporate,
                                unselectedIconColor = SlateMutedText,
                                unselectedTextColor = SlateMutedText
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val isHome = currentTab == 0
            if (isHome) {
                ExtendedFloatingActionButton(
                    onClick = {
                        mainViewModel.setTab(2)
                    },
                    icon = {
                        Icon(
                            Icons.Default.ElectricBolt,
                            contentDescription = "Post Urgent",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(
                            "Sell Fast",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    containerColor = BlueCorporate,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_sell_fast")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = QuickNestRoute.Home,
                modifier = Modifier.fillMaxSize()
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
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }

    // Modal Sheets & Dialogs
    selectedProperty?.let { prop ->
        PropertyDetailSheet(
            property = prop,
            onDismiss = { mainViewModel.closePropertyDetails() },
            onToggleSave = { mainViewModel.toggleSave(prop) },
            onOpenChat = {
                mainViewModel.closePropertyDetails()
                mainViewModel.openChat(prop)
            },
            onOpenVisitBooking = {
                mainViewModel.closePropertyDetails()
                mainViewModel.openVisitBooking(prop)
            },
            onOpenQuickMatch = {
                mainViewModel.openQuickMatch(prop)
            },
            onOpenReport = {
                mainViewModel.openReport(prop)
            }
        )
    }

    quickMatchProperty?.let { prop ->
        QuickMatchDialog(
            property = prop,
            matches = mainViewModel.getMatchesForProperty(prop),
            onDismiss = { mainViewModel.closeQuickMatch() }
        )
    }

    contactSellerProperty?.let { prop ->
        ContactSellerDialog(
            property = prop,
            onStartChat = { initialMsg ->
                mainViewModel.startChatFromContactSeller(prop, initialMsg)
            },
            onSendEmailInquiry = { subject, message, buyerEmail, buyerPhone ->
                mainViewModel.sendEmailInquiry(prop, subject, message, buyerEmail, buyerPhone)
            },
            onDismiss = { mainViewModel.closeContactSeller() }
        )
    }

    chatProperty?.let { prop ->
        ChatDialog(
            property = prop,
            messages = chatMessages,
            onSendMessage = { mainViewModel.sendChatMessage(it) },
            onDismiss = { mainViewModel.closeChat() },
            onOpenVisitBooking = {
                mainViewModel.openVisitBooking(prop)
            }
        )
    }

    visitProperty?.let { prop ->
        VisitBookingDialog(
            property = prop,
            onConfirm = { date, time ->
                mainViewModel.confirmVisit(prop, date, time)
            },
            onDismiss = { mainViewModel.closeVisitBooking() }
        )
    }

    reportProperty?.let { prop ->
        ReportDialog(
            property = prop,
            onSubmitReport = { reason, details ->
                mainViewModel.submitReport(prop, reason, details)
            },
            onDismiss = { mainViewModel.closeReport() }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            selectedCategory = selectedCategory,
            selectedPropertyType = selectedPropertyType,
            selectedBudget = selectedBudget,
            selectedBedrooms = selectedBedrooms,
            selectedSortOption = selectedSortOption,
            verifiedOnly = verifiedOnly,
            urgentOnly = urgentOnly,
            onCategoryChange = { homeViewModel.selectCategory(it) },
            onPropertyTypeChange = { homeViewModel.selectPropertyType(it) },
            onBudgetChange = { homeViewModel.setBudgetFilter(it) },
            onBedroomsChange = { homeViewModel.setBedroomsFilter(it) },
            onSortChange = { homeViewModel.setSortOption(it) },
            onVerifiedOnlyChange = { homeViewModel.setVerifiedOnly(it) },
            onUrgentOnlyChange = { homeViewModel.setUrgentOnly(it) },
            onResetFilters = { homeViewModel.resetFilters() },
            onApply = { mainViewModel.closeFilterSheet() },
            onDismiss = { mainViewModel.closeFilterSheet() }
        )
    }

    if (showAiAssistant) {
        AiAssistantDialog(
            query = aiQuery,
            results = aiResults,
            explanation = aiExplanation,
            onSearch = { mainViewModel.runAiNaturalSearch(it) },
            onSelectProperty = { mainViewModel.openPropertyDetails(it) },
            onDismiss = { mainViewModel.closeAiAssistant() }
        )
    }

    if (showSmartMatchDialog) {
        SmartMatchDialog(
            viewModel = legacyViewModel,
            onSelectProperty = { mainViewModel.openPropertyDetails(it) },
            onDismiss = { mainViewModel.closeSmartMatchDialog() }
        )
    }
}

// Backward-compatible overload for existing tests
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNestApp(viewModel: QuickNestViewModel) {
    QuickNestApp(legacyViewModel = viewModel)
}
