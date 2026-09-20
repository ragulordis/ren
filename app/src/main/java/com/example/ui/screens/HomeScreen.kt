package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppNotification
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.ui.components.NotificationFeedSection
import com.example.ui.components.PropertyCard
import com.example.ui.components.PropertySearchBar
import com.example.ui.components.RecentSearchesRow
import com.example.ui.components.SkeletonPropertyCard
import com.example.ui.components.SkeletonUrgentPropertyCard
import com.example.ui.components.SmartMatchCard
import com.example.ui.components.UrgentPropertyCard
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CoolCyanGradient
import com.example.ui.theme.CoolHeroGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.UrgencyFlameContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.BlueCorporate
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.SurfaceIvoryTint
import com.example.ui.theme.CharcoalNavyText
import com.example.ui.theme.SlateSecondaryText
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.QuickNestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProperties by homeViewModel.filteredProperties.collectAsStateWithLifecycle()
    val urgentProperties by homeViewModel.urgentProperties.collectAsStateWithLifecycle()
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by homeViewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchQuery by homeViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by homeViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPropertyType by homeViewModel.selectedPropertyType.collectAsStateWithLifecycle()
    val selectedLocation by homeViewModel.selectedLocation.collectAsStateWithLifecycle()
    val urgentOnly by homeViewModel.urgentOnly.collectAsStateWithLifecycle()
    val activeFiltersCount by homeViewModel.activeFiltersCount.collectAsStateWithLifecycle()
    val selectedBudget by homeViewModel.selectedBudget.collectAsStateWithLifecycle()
    val selectedBedrooms by homeViewModel.selectedBedrooms.collectAsStateWithLifecycle()
    val verifiedOnly by homeViewModel.verifiedOnly.collectAsStateWithLifecycle()
    val minPrice by homeViewModel.minPrice.collectAsStateWithLifecycle()
    val maxPrice by homeViewModel.maxPrice.collectAsStateWithLifecycle()
    val recentSearches by homeViewModel.recentSearches.collectAsStateWithLifecycle()
    val smartMatchResults by homeViewModel.smartMatchResults.collectAsStateWithLifecycle()
    val smartMatchPreferences by homeViewModel.smartMatchPreferences.collectAsStateWithLifecycle()
    val isSmartMatchLoading by homeViewModel.isSmartMatchLoading.collectAsStateWithLifecycle()
    val currentUserProfile by homeViewModel.currentUserProfile.collectAsStateWithLifecycle()
    val notifications by mainViewModel.allNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by mainViewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    HomeScreenContent(
        notifications = notifications,
        unreadNotificationsCount = unreadNotificationsCount,
        onOpenNotificationCenter = { mainViewModel.openNotificationCenter() },
        filteredProperties = filteredProperties,
        urgentProperties = urgentProperties,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        selectedPropertyType = selectedPropertyType,
        selectedLocation = selectedLocation,
        urgentOnly = urgentOnly,
        activeFiltersCount = activeFiltersCount,
        selectedBudget = selectedBudget,
        selectedBedrooms = selectedBedrooms,
        verifiedOnly = verifiedOnly,
        minPrice = minPrice,
        maxPrice = maxPrice,
        recentSearches = recentSearches,
        smartMatchResults = smartMatchResults,
        smartMatchPreferences = smartMatchPreferences,
        isSmartMatchLoading = isSmartMatchLoading,
        currentUserProfile = currentUserProfile,
        onSelectLocation = { homeViewModel.selectLocation(it) },
        onRefresh = { homeViewModel.refreshProperties() },
        onSearchQueryChange = { homeViewModel.updateSearchQuery(it) },
        onPriceRangeChange = { min, max -> homeViewModel.setPriceRange(min, max) },
        onOpenFilterSheet = { mainViewModel.openFilterSheet() },
        onSearchSubmitted = { homeViewModel.saveRecentSearch(it) },
        onSelectPropertyType = { homeViewModel.selectPropertyType(it) },
        onClearPriceRange = { homeViewModel.clearPriceRange() },
        onSelectCategory = { homeViewModel.selectCategory(it) },
        onSetBudgetFilter = { homeViewModel.setBudgetFilter(it) },
        onSetBedroomsFilter = { homeViewModel.setBedroomsFilter(it) },
        onSetUrgentOnly = { homeViewModel.setUrgentOnly(it) },
        onSetVerifiedOnly = { homeViewModel.setVerifiedOnly(it) },
        onResetFilters = { homeViewModel.resetFilters() },
        onOpenAiAssistant = { mainViewModel.openAiAssistant() },
        onOpenSmartMatchDialog = { mainViewModel.openSmartMatchDialog() },
        onOpenPropertyDetails = { mainViewModel.openPropertyDetails(it) },
        onToggleSave = { homeViewModel.toggleSave(it) },
        onContactSeller = { mainViewModel.openContactSeller(it) },
        onBookVisit = { mainViewModel.openVisitBooking(it) },
        onToggleUrgentOnly = { homeViewModel.toggleUrgentOnly() },
        onRemoveRecentSearch = { homeViewModel.removeRecentSearch(it) },
        onClearRecentSearches = { homeViewModel.clearRecentSearches() },
        onNavigateToExplore = { mainViewModel.setTab(1) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuickNestViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val urgentProperties by viewModel.urgentProperties.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPropertyType by viewModel.selectedPropertyType.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val urgentOnly by viewModel.urgentOnly.collectAsStateWithLifecycle()
    val activeFiltersCount by viewModel.activeFiltersCount.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val selectedBedrooms by viewModel.selectedBedrooms.collectAsStateWithLifecycle()
    val verifiedOnly by viewModel.verifiedOnly.collectAsStateWithLifecycle()
    val minPrice by viewModel.minPrice.collectAsStateWithLifecycle()
    val maxPrice by viewModel.maxPrice.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val smartMatchResults by viewModel.smartMatchResults.collectAsStateWithLifecycle()
    val smartMatchPreferences by viewModel.smartMatchPreferences.collectAsStateWithLifecycle()
    val isSmartMatchLoading by viewModel.isSmartMatchLoading.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    HomeScreenContent(
        filteredProperties = filteredProperties,
        urgentProperties = urgentProperties,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        selectedPropertyType = selectedPropertyType,
        selectedLocation = selectedLocation,
        urgentOnly = urgentOnly,
        activeFiltersCount = activeFiltersCount,
        selectedBudget = selectedBudget,
        selectedBedrooms = selectedBedrooms,
        verifiedOnly = verifiedOnly,
        minPrice = minPrice,
        maxPrice = maxPrice,
        recentSearches = recentSearches,
        smartMatchResults = smartMatchResults,
        smartMatchPreferences = smartMatchPreferences,
        isSmartMatchLoading = isSmartMatchLoading,
        currentUserProfile = currentUserProfile,
        onSelectLocation = { viewModel.selectLocation(it) },
        onRefresh = { viewModel.refreshProperties() },
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onPriceRangeChange = { min, max -> viewModel.setPriceRange(min, max) },
        onOpenFilterSheet = { viewModel.openFilterSheet() },
        onSearchSubmitted = { viewModel.saveRecentSearch(it) },
        onSelectPropertyType = { viewModel.selectPropertyType(it) },
        onClearPriceRange = { viewModel.clearPriceRange() },
        onSelectCategory = { viewModel.selectCategory(it) },
        onSetBudgetFilter = { viewModel.setBudgetFilter(it) },
        onSetBedroomsFilter = { viewModel.setBedroomsFilter(it) },
        onSetUrgentOnly = { viewModel.setUrgentOnly(it) },
        onSetVerifiedOnly = { viewModel.setVerifiedOnly(it) },
        onResetFilters = { viewModel.resetFilters() },
        onOpenAiAssistant = { viewModel.openAiAssistant() },
        onOpenSmartMatchDialog = { viewModel.openSmartMatchDialog() },
        onOpenPropertyDetails = { viewModel.openPropertyDetails(it) },
        onToggleSave = { viewModel.toggleSave(it) },
        onContactSeller = { viewModel.openContactSeller(it) },
        onBookVisit = { viewModel.openVisitBooking(it) },
        onToggleUrgentOnly = { viewModel.toggleUrgentOnly() },
        onRemoveRecentSearch = { viewModel.removeRecentSearch(it) },
        onClearRecentSearches = { viewModel.clearRecentSearches() },
        onNavigateToExplore = { viewModel.setTab(1) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    filteredProperties: List<Property>,
    urgentProperties: List<Property>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    searchQuery: String,
    selectedCategory: PropertyCategory,
    selectedPropertyType: String?,
    selectedLocation: String,
    urgentOnly: Boolean,
    activeFiltersCount: Int,
    selectedBudget: com.example.data.model.BudgetFilter,
    selectedBedrooms: Int,
    verifiedOnly: Boolean,
    minPrice: Long?,
    maxPrice: Long?,
    recentSearches: List<String>,
    smartMatchResults: List<com.example.data.model.SmartMatchResult>,
    smartMatchPreferences: com.example.data.model.UserPreferences,
    isSmartMatchLoading: Boolean,
    currentUserProfile: com.example.data.model.UserProfile?,
    notifications: List<AppNotification> = emptyList(),
    unreadNotificationsCount: Int = 0,
    onOpenNotificationCenter: () -> Unit = {},
    onSelectLocation: (String) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPriceRangeChange: (Long?, Long?) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onSelectPropertyType: (String?) -> Unit,
    onClearPriceRange: () -> Unit,
    onSelectCategory: (PropertyCategory) -> Unit,
    onSetBudgetFilter: (com.example.data.model.BudgetFilter) -> Unit,
    onSetBedroomsFilter: (Int) -> Unit,
    onSetUrgentOnly: (Boolean) -> Unit,
    onSetVerifiedOnly: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenSmartMatchDialog: () -> Unit,
    onOpenPropertyDetails: (Property) -> Unit,
    onToggleSave: (Property) -> Unit,
    onContactSeller: (Property) -> Unit,
    onBookVisit: (Property) -> Unit,
    onToggleUrgentOnly: () -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {

    var showLocationPicker by remember { mutableStateOf(false) }
    val locationsList = listOf(
        "All Locations", "Chennai", "Bengaluru", "Mumbai", "Delhi NCR",
        "Hyderabad", "Pune", "Kochi", "Goa", "Pondicherry", "Kottakuppam",
        "Coimbatore", "Ahmedabad", "Jaipur", "Kolkata"
    )

    if (showLocationPicker) {
        com.example.ui.components.IndiaLocationPickerDialog(
            selectedLocation = selectedLocation,
            onLocationSelected = { onSelectLocation(it) },
            onDismissRequest = { showLocationPicker = false }
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { onRefresh() },
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_swipe_refresh")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Top Header: Greeting & Pan-India Location Picker
        item {
            val livePulseTransition = rememberInfiniteTransition(label = "livePulse")
            val livePulseAlpha by livePulseTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "livePulseAlpha"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Good Day,",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentUserProfile?.displayName?.ifBlank { "Explorer" } ?: "Explorer"} 👋",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Location Indicator & Picker on right with live radar beacon
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                            .clickable { showLocationPicker = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("location_selector")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = selectedLocation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = livePulseAlpha), CircleShape)
                            )
                        }
                        Text(
                            text = "INDIA 🇮🇳",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 2. Search Box with Location & Price Range Filters + Natural AI Prompt
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                PropertySearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { onSearchQueryChange(it) },
                    selectedLocation = selectedLocation,
                    onLocationChange = { onSelectLocation(it) },
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    onPriceRangeChange = { min, max -> onPriceRangeChange(min, max) },
                    availableLocations = locationsList,
                    onOpenFilterSheet = { onOpenFilterSheet() },
                    activeFiltersCount = activeFiltersCount,
                    onSearchSubmitted = { term -> onSearchSubmitted(term) }
                )

                // Row of Property Type Filter Chips (Apartment, Villa, Studio, House, Plot/Land, Commercial)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("property_type_filter_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val propertyTypeItems = listOf(
                        Triple("All", "All Types", "✨"),
                        Triple("Apartment", "Apartment", "🏢"),
                        Triple("Villa", "Villa", "🏡"),
                        Triple("Studio", "Studio", "🛋️"),
                        Triple("House", "House", "🏠"),
                        Triple("Plot / Land", "Plot / Land", "🌳"),
                        Triple("Commercial", "Commercial", "🏬")
                    )

                    propertyTypeItems.forEach { (typeKey, label, emoji) ->
                        val isSelected = (selectedPropertyType == null && typeKey == "All") || (selectedPropertyType == typeKey)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onSelectPropertyType(if (typeKey == "All") null else typeKey)
                            },
                            leadingIcon = {
                                Text(
                                    text = emoji,
                                    fontSize = 13.sp
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NavyPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = SurfaceWhite,
                                labelColor = CharcoalNavyText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NavyPrimary else CardBorder
                            ),
                            modifier = Modifier.testTag("filter_chip_${typeKey.lowercase().replace(" ", "_").replace("/", "_")}")
                        )
                    }
                }

                // Active Filters Row (if any active)
                if (activeFiltersCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedPropertyType != null) {
                            FilterChip(
                                selected = true,
                                onClick = { onSelectPropertyType(null) },
                                label = { Text("$selectedPropertyType ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("active_filter_chip_property_type")
                            )
                        }
                        if (minPrice != null || maxPrice != null) {
                            val rangeText = when {
                                minPrice != null && maxPrice != null -> "₹${minPrice!! / 100000}L - ₹${maxPrice!! / 100000}L ✕"
                                minPrice != null -> "Min ₹${minPrice!! / 100000}L ✕"
                                else -> "Max ₹${maxPrice!! / 100000}L ✕"
                            }
                            FilterChip(
                                selected = true,
                                onClick = { onClearPriceRange() },
                                label = { Text(rangeText, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                        if (selectedLocation != "All Locations" && selectedLocation != "Kottakuppam") {
                            FilterChip(
                                selected = true,
                                onClick = { onSelectLocation("All Locations") },
                                label = { Text("$selectedLocation ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        if (selectedCategory != PropertyCategory.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { onSelectCategory(PropertyCategory.ALL) },
                                label = { Text("${selectedCategory.label} ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        if (selectedBudget != com.example.viewmodel.BudgetFilter.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { onSetBudgetFilter(com.example.viewmodel.BudgetFilter.ALL) },
                                label = { Text("${selectedBudget.label} ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                        if (selectedBedrooms > 0) {
                            FilterChip(
                                selected = true,
                                onClick = { onSetBedroomsFilter(0) },
                                label = { Text("${selectedBedrooms} BHK ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            )
                        }
                        if (urgentOnly) {
                            FilterChip(
                                selected = true,
                                onClick = { onSetUrgentOnly(false) },
                                label = { Text("⚡ Urgent ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UrgencyFlameContainer,
                                    selectedLabelColor = UrgencyFlame
                                )
                            )
                        }
                        if (verifiedOnly) {
                            FilterChip(
                                selected = true,
                                onClick = { onSetVerifiedOnly(false) },
                                label = { Text("🛡️ Verified ✕", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.example.ui.theme.VerifiedGreenContainer,
                                    selectedLabelColor = com.example.ui.theme.VerifiedGreen
                                )
                            )
                        }

                        Text(
                            text = "Clear All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onResetFilters() }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AI Property Assistant Bar with interactive press and cyber-luxe gradient
                val aiInteractionSource = remember { MutableInteractionSource() }
                val isAiPressed by aiInteractionSource.collectIsPressedAsState()
                val aiScale by animateFloatAsState(
                    targetValue = if (isAiPressed) 0.97f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "aiBannerScale"
                )

                val aiGlowTransition = rememberInfiniteTransition(label = "aiGlow")
                val aiGlowAlpha by aiGlowTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "aiGlowAlpha"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(aiScale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(NavyPrimary)
                        .border(
                            1.dp,
                            AccentGold.copy(alpha = aiGlowAlpha * 0.7f),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable(
                            interactionSource = aiInteractionSource,
                            indication = null
                        ) { onOpenAiAssistant() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("ai_assistant_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AccentGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Ren AI Advisory",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Instant valuation & intelligent matching",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentGold)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Consult AI →",
                            color = NavyPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 🔔 NOTIFICATION SYSTEM FEED (Replaced Smart Match Algorithm)
        item(key = "notifications_feed_section") {
            NotificationFeedSection(
                notifications = notifications,
                unreadCount = unreadNotificationsCount,
                onOpenNotificationCenter = onOpenNotificationCenter,
                onNotificationClick = { notification ->
                    if (notification.propertyId != null) {
                        val prop = filteredProperties.firstOrNull { it.id == notification.propertyId }
                            ?: urgentProperties.firstOrNull { it.id == notification.propertyId }
                        if (prop != null) {
                            onOpenPropertyDetails(prop)
                        } else {
                            onOpenNotificationCenter()
                        }
                    } else {
                        onOpenNotificationCenter()
                    }
                }
            )
        }

        // 3. 🔥 URGENT PROPERTIES (Section 6 & 7 of Blueprint)
        if (isLoading && urgentProperties.isEmpty()) {
            item(key = "skeleton_urgent_row") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "URGENT PROPERTIES",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.testTag("home_skeleton_urgent_row")
                    ) {
                        items(3) { idx ->
                            SkeletonUrgentPropertyCard(
                                modifier = Modifier.testTag("home_skeleton_urgent_$idx")
                            )
                        }
                    }
                }
            }
        } else if (urgentProperties.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "URGENT PROPERTIES",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = if (urgentOnly) "Show All" else "View All",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onToggleUrgentOnly() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(urgentProperties, key = { "urgent_${it.id}" }) { property ->
                            UrgentPropertyCard(
                                property = property,
                                onClick = { onOpenPropertyDetails(property) },
                                onToggleSave = { onToggleSave(property) },
                                onContactSeller = { onContactSeller(property) }
                            )
                        }
                    }
                }
            }
        }

        // 4. POPULAR CATEGORIES (Professional Polish Quick Categories)
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "QUICK CATEGORIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PropertyCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val catInteraction = remember { MutableInteractionSource() }
                        val isCatPressed by catInteraction.collectIsPressedAsState()
                        val catScale by animateFloatAsState(
                            targetValue = if (isCatPressed) 0.94f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "catScale_${cat.name}"
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) NavyPrimary else SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NavyPrimary else CardBorder
                            ),
                            modifier = Modifier
                                .scale(catScale)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(
                                    interactionSource = catInteraction,
                                    indication = null
                                ) { onSelectCategory(cat) }
                                .testTag("category_chip_${cat.name}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(78.dp)
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            if (isSelected) AccentGold.copy(alpha = 0.25f) else SurfaceIvoryTint,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat.iconEmoji, fontSize = 18.sp)
                                }
                                Text(
                                    text = cat.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else CharcoalNavyText
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Active Filters Quick Pill (Urgent Only toggle)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = urgentOnly,
                    onClick = { onToggleUrgentOnly() },
                    label = { Text("🔥 Urgent Only (1-7 Days)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UrgencyFlameContainer,
                        selectedLabelColor = UrgencyFlame
                    ),
                    modifier = Modifier.testTag("filter_urgent_toggle")
                )
            }
        }

        // 5b. Recent Searches Row above property list
        if (recentSearches.isNotEmpty()) {
            item {
                RecentSearchesRow(
                    recentSearches = recentSearches,
                    currentQuery = searchQuery,
                    onSelectSearch = { term ->
                        onSearchQueryChange(term)
                    },
                    onRemoveSearch = { term ->
                        onRemoveRecentSearch(term)
                    },
                    onClearAll = {
                        onClearRecentSearches()
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }

        // 6. NEAR YOU (Local Discovery Cards)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEAR ${selectedLocation.uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "See Map",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToExplore() }
                )
            }
        }

        // List of Property Cards
        if (isLoading && filteredProperties.isEmpty()) {
            items(3) { idx ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SkeletonPropertyCard(
                        modifier = Modifier.testTag("home_skeleton_property_card_$idx")
                    )
                }
            }
        } else if (filteredProperties.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 36.sp)
                        Text(
                            text = "No properties found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Try adjusting your search or switching category",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredProperties, key = { it.id }) { property ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    PropertyCard(
                        property = property,
                        onClick = { onOpenPropertyDetails(property) },
                        onToggleSave = { onToggleSave(property) }
                    )
                }
            }
        }
    }
}
}
