package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.viewmodel.QuickNestViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QuickNestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                QuickNestApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickNestApp(viewModel: QuickNestViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedProperty by viewModel.selectedProperty.collectAsStateWithLifecycle()
    val contactSellerProperty by viewModel.contactSellerProperty.collectAsStateWithLifecycle()
    val chatProperty by viewModel.chatProperty.collectAsStateWithLifecycle()
    val visitProperty by viewModel.visitProperty.collectAsStateWithLifecycle()
    val quickMatchProperty by viewModel.quickMatchProperty.collectAsStateWithLifecycle()
    val showAiAssistant by viewModel.showAiAssistant.collectAsStateWithLifecycle()
    val reportProperty by viewModel.reportProperty.collectAsStateWithLifecycle()
    val showFilterSheet by viewModel.showFilterSheet.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPropertyType by viewModel.selectedPropertyType.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val selectedBedrooms by viewModel.selectedBedrooms.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val verifiedOnly by viewModel.verifiedOnly.collectAsStateWithLifecycle()
    val urgentOnly by viewModel.urgentOnly.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val aiQuery by viewModel.aiQuery.collectAsStateWithLifecycle()
    val aiResults by viewModel.aiResults.collectAsStateWithLifecycle()
    val aiExplanation by viewModel.aiExplanation.collectAsStateWithLifecycle()
    val showSmartMatchDialog by viewModel.showSmartMatchDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearFeedbackMessage()
        }
    }

    val navItems = listOf(
        Triple("Home", Icons.Default.Home, "nav_home"),
        Triple("Explore", Icons.Default.Explore, "nav_explore"),
        Triple("Post", Icons.Default.PostAdd, "nav_post"),
        Triple("Saved", Icons.Default.Favorite, "nav_saved"),
        Triple("Profile", Icons.Default.Person, "nav_profile")
    )

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
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CoolHeroGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "QuickNest Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "QuickNest",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = UrgencyFlame.copy(alpha = 0.14f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, UrgencyFlame.copy(alpha = 0.35f)),
                                    modifier = Modifier.scale(pulseScale)
                                ) {
                                    Text(
                                        text = "FAST DEALS",
                                        color = UrgencyFlame,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Hyperlocal Property Network",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openSmartMatchDialog() },
                        modifier = Modifier
                            .padding(end = 3.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
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
                        onClick = { viewModel.openAiAssistant() },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Property Matcher",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .border(width = 1.dp, brush = CoolGlassmorphicBorder, shape = RectangleShape)
                    .testTag("bottom_navigation_bar")
            ) {
                navItems.forEachIndexed { index, (label, icon, testTag) ->
                    val isSelected = currentTab == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.18f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "nav_scale_$index"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.testTag(testTag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setTab(2) }, // Navigate to Post
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
                    containerColor = MaterialTheme.colorScheme.primary,
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
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> (width * 0.25f).toInt() } + fadeIn(animationSpec = tween(240)))
                            .togetherWith(slideOutHorizontally { width -> (-width * 0.25f).toInt() } + fadeOut(animationSpec = tween(180)))
                    } else {
                        (slideInHorizontally { width -> (-width * 0.25f).toInt() } + fadeIn(animationSpec = tween(240)))
                            .togetherWith(slideOutHorizontally { width -> (width * 0.25f).toInt() } + fadeOut(animationSpec = tween(180)))
                    }
                },
                label = "ScreenTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> HomeScreen(viewModel = viewModel)
                    1 -> ExploreScreen(viewModel = viewModel)
                    2 -> PostPropertyScreen(viewModel = viewModel)
                    3 -> SavedScreen(viewModel = viewModel)
                    4 -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Sheets & Dialogs
    selectedProperty?.let { prop ->
        PropertyDetailSheet(
            property = prop,
            onDismiss = { viewModel.closePropertyDetails() },
            onToggleSave = { viewModel.toggleSave(prop) },
            onOpenChat = {
                viewModel.closePropertyDetails()
                viewModel.openChat(prop)
            },
            onOpenVisitBooking = {
                viewModel.closePropertyDetails()
                viewModel.openVisitBooking(prop)
            },
            onOpenQuickMatch = {
                viewModel.openQuickMatch(prop)
            },
            onOpenReport = {
                viewModel.openReport(prop)
            }
        )
    }

    quickMatchProperty?.let { prop ->
        QuickMatchDialog(
            property = prop,
            matches = viewModel.getMatchesForProperty(prop),
            onDismiss = { viewModel.closeQuickMatch() }
        )
    }

    contactSellerProperty?.let { prop ->
        ContactSellerDialog(
            property = prop,
            onStartChat = { initialMsg ->
                viewModel.startChatFromContactSeller(prop, initialMsg)
            },
            onSendEmailInquiry = { subject, message, buyerEmail, buyerPhone ->
                viewModel.sendEmailInquiry(prop, subject, message, buyerEmail, buyerPhone)
            },
            onDismiss = { viewModel.closeContactSeller() }
        )
    }

    chatProperty?.let { prop ->
        ChatDialog(
            property = prop,
            messages = chatMessages,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onDismiss = { viewModel.closeChat() },
            onOpenVisitBooking = {
                viewModel.openVisitBooking(prop)
            }
        )
    }

    visitProperty?.let { prop ->
        VisitBookingDialog(
            property = prop,
            onConfirm = { date, time ->
                viewModel.confirmVisit(prop, date, time)
            },
            onDismiss = { viewModel.closeVisitBooking() }
        )
    }

    reportProperty?.let { prop ->
        ReportDialog(
            property = prop,
            onSubmitReport = { reason, details ->
                viewModel.submitReport(prop, reason, details)
            },
            onDismiss = { viewModel.closeReport() }
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
            onCategoryChange = { viewModel.selectCategory(it) },
            onPropertyTypeChange = { viewModel.selectPropertyType(it) },
            onBudgetChange = { viewModel.setBudgetFilter(it) },
            onBedroomsChange = { viewModel.setBedroomsFilter(it) },
            onSortChange = { viewModel.setSortOption(it) },
            onVerifiedOnlyChange = { viewModel.setVerifiedOnly(it) },
            onUrgentOnlyChange = { viewModel.setUrgentOnly(it) },
            onResetFilters = { viewModel.resetFilters() },
            onApply = { viewModel.closeFilterSheet() },
            onDismiss = { viewModel.closeFilterSheet() }
        )
    }

    if (showAiAssistant) {
        AiAssistantDialog(
            query = aiQuery,
            results = aiResults,
            explanation = aiExplanation,
            onSearch = { viewModel.runAiNaturalSearch(it) },
            onSelectProperty = { viewModel.openPropertyDetails(it) },
            onDismiss = { viewModel.closeAiAssistant() }
        )
    }

    if (showSmartMatchDialog) {
        SmartMatchDialog(
            viewModel = viewModel,
            onSelectProperty = { viewModel.openPropertyDetails(it) },
            onDismiss = { viewModel.closeSmartMatchDialog() }
        )
    }
}
