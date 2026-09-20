package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.ui.components.RecentSearchesRow
import com.example.ui.components.SkeletonPropertyList
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.viewmodel.BudgetFilter
import com.example.viewmodel.QuickNestViewModel
import com.example.viewmodel.SortOption

/**
 * Material 3 PropertyList Composable.
 *
 * Observes property listings populated from the local Room database
 * and displays them efficiently using a LazyColumn. Integrates seamlessly
 * with the active search, location, budget, price range, and category filter states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyList(
    viewModel: QuickNestViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
    headerItems: (LazyListScope.() -> Unit)? = null,
    showFilterSummaryHeader: Boolean = true,
    onPropertyClick: (Property) -> Unit = { viewModel.openPropertyDetails(it) },
    onToggleSave: (Property) -> Unit = { viewModel.toggleSave(it) },
    onContactSeller: (Property) -> Unit = { viewModel.openContactSeller(it) }
) {
    // Observe reactive state derived from Room database through ViewModel
    val properties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPropertyType by viewModel.selectedPropertyType.collectAsStateWithLifecycle()
    val minPrice by viewModel.minPrice.collectAsStateWithLifecycle()
    val maxPrice by viewModel.maxPrice.collectAsStateWithLifecycle()
    val selectedBudget by viewModel.selectedBudget.collectAsStateWithLifecycle()
    val selectedSortOption by viewModel.selectedSortOption.collectAsStateWithLifecycle()
    val urgentOnly by viewModel.urgentOnly.collectAsStateWithLifecycle()
    val verifiedOnly by viewModel.verifiedOnly.collectAsStateWithLifecycle()
    val activeFiltersCount by viewModel.activeFiltersCount.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    PropertyListContent(
        properties = properties,
        totalAvailableCount = allProperties.size,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        isSyncingCloud = isSyncingCloud,
        searchQuery = searchQuery,
        selectedLocation = selectedLocation,
        selectedCategory = selectedCategory,
        selectedPropertyType = selectedPropertyType,
        minPrice = minPrice,
        maxPrice = maxPrice,
        selectedBudget = selectedBudget,
        selectedSortOption = selectedSortOption,
        urgentOnly = urgentOnly,
        verifiedOnly = verifiedOnly,
        activeFiltersCount = activeFiltersCount,
        modifier = modifier.testTag("property_list_view"),
        listState = listState,
        contentPadding = contentPadding,
        headerItems = headerItems,
        showFilterSummaryHeader = showFilterSummaryHeader,
        onPropertyClick = onPropertyClick,
        onToggleSave = onToggleSave,
        onContactSeller = onContactSeller,
        onRefresh = { viewModel.refreshProperties() },
        onResetFilters = { viewModel.resetAllFilters() },
        onOpenFilterSheet = { viewModel.openFilterSheet() },
        onClearQuery = { viewModel.updateSearchQuery("") },
        onClearLocation = { viewModel.selectLocation("All Locations") },
        onClearCategory = { viewModel.selectCategory(PropertyCategory.ALL) },
        onClearPropertyType = { viewModel.selectPropertyType(null) },
        onClearPriceRange = { viewModel.clearPriceRange() },
        onToggleUrgentOnly = { viewModel.toggleUrgentOnly() },
        recentSearches = recentSearches,
        onSelectRecentSearch = { viewModel.updateSearchQuery(it) },
        onRemoveRecentSearch = { viewModel.removeRecentSearch(it) },
        onClearRecentSearches = { viewModel.clearRecentSearches() }
    )
}

/**
 * Stateless variant of PropertyList for flexible reuse and testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListContent(
    properties: List<Property>,
    totalAvailableCount: Int,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    isSyncingCloud: Boolean = false,
    searchQuery: String = "",
    selectedLocation: String = "All Locations",
    selectedCategory: PropertyCategory = PropertyCategory.ALL,
    selectedPropertyType: String? = null,
    minPrice: Long? = null,
    maxPrice: Long? = null,
    selectedBudget: BudgetFilter = BudgetFilter.ALL,
    selectedSortOption: SortOption = SortOption.URGENCY,
    urgentOnly: Boolean = false,
    verifiedOnly: Boolean = false,
    activeFiltersCount: Int = 0,
    recentSearches: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    headerItems: (LazyListScope.() -> Unit)? = null,
    showFilterSummaryHeader: Boolean = true,
    onPropertyClick: (Property) -> Unit = {},
    onToggleSave: (Property) -> Unit = {},
    onContactSeller: (Property) -> Unit = {},
    onRefresh: () -> Unit = {},
    onResetFilters: () -> Unit = {},
    onOpenFilterSheet: () -> Unit = {},
    onClearQuery: () -> Unit = {},
    onClearLocation: () -> Unit = {},
    onClearCategory: () -> Unit = {},
    onClearPropertyType: () -> Unit = {},
    onClearPriceRange: () -> Unit = {},
    onToggleUrgentOnly: () -> Unit = {},
    onSelectRecentSearch: (String) -> Unit = {},
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {}
) {
    val hasFilters = activeFiltersCount > 0 || searchQuery.isNotBlank() || (selectedLocation != "All Locations" && selectedLocation != "Kottakuppam")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize().testTag("property_list_swipe_refresh")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Optional custom header items injected before property items
        headerItems?.invoke(this)

        // Loading overlay banner when actively re-fetching/filtering with existing items
        if (isLoading && properties.isNotEmpty()) {
            item(key = "property_list_refresh_loading") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("circular_progress_indicator_sync"),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Updating property listings from database & cloud...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Recent Searches horizontal row
        if (recentSearches.isNotEmpty()) {
            item(key = "property_list_recent_searches_row") {
                RecentSearchesRow(
                    recentSearches = recentSearches,
                    currentQuery = searchQuery,
                    onSelectSearch = onSelectRecentSearch,
                    onRemoveSearch = onRemoveRecentSearch,
                    onClearAll = onClearRecentSearches,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // Results Header / Filter summary
        if (showFilterSummaryHeader) {
            item(key = "property_list_header_summary") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (selectedLocation != "All Locations") "PROPERTIES IN ${selectedLocation.uppercase()}" else "AVAILABLE PROPERTIES",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .testTag("circular_progress_indicator_mini"),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "${properties.size} of $totalAvailableCount listings matching criteria",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Filter Action Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderSubtle),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenFilterSheet() }
                                .testTag("property_list_filter_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (activeFiltersCount > 0) "Filter ($activeFiltersCount)" else "Filter",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Active Filters Chips Bar
                    if (hasFilters) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (searchQuery.isNotBlank()) {
                                FilterChip(
                                    selected = true,
                                    onClick = onClearQuery,
                                    label = { Text("\"$searchQuery\" ✕", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("chip_active_query")
                                )
                            }

                            if (selectedCategory != PropertyCategory.ALL) {
                                FilterChip(
                                    selected = true,
                                    onClick = onClearCategory,
                                    label = { Text("${selectedCategory.label} ✕", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.testTag("chip_active_category")
                                )
                            }

                            if (!selectedPropertyType.isNullOrBlank()) {
                                FilterChip(
                                    selected = true,
                                    onClick = onClearPropertyType,
                                    label = { Text("$selectedPropertyType ✕", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag("chip_active_property_type")
                                )
                            }

                            if (minPrice != null || maxPrice != null) {
                                val priceText = when {
                                    minPrice != null && maxPrice != null -> "₹${minPrice / 100000}L - ₹${maxPrice / 100000}L ✕"
                                    minPrice != null -> "Min ₹${minPrice / 100000}L ✕"
                                    else -> "Max ₹${maxPrice!! / 100000}L ✕"
                                }
                                FilterChip(
                                    selected = true,
                                    onClick = onClearPriceRange,
                                    label = { Text(priceText, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    modifier = Modifier.testTag("chip_active_price")
                                )
                            }

                            if (urgentOnly) {
                                FilterChip(
                                    selected = true,
                                    onClick = onToggleUrgentOnly,
                                    label = { Text("Urgent only ✕", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = UrgencyFlame.copy(alpha = 0.2f),
                                        selectedLabelColor = UrgencyFlame
                                    ),
                                    modifier = Modifier.testTag("chip_active_urgent")
                                )
                            }

                            // Quick Reset All text button
                            Text(
                                text = "Clear All",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clickable { onResetFilters() }
                                    .padding(horizontal = 8.dp)
                                    .testTag("clear_all_filters_button")
                            )
                        }
                    }
                }
            }
        }

        // Loading or Empty State
        if (isLoading && properties.isEmpty()) {
            item(key = "property_list_loading_state") {
                PropertyListLoadingState(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (properties.isEmpty()) {
            item(key = "property_list_empty_state") {
                PropertyListEmptyState(
                    searchQuery = searchQuery,
                    selectedLocation = selectedLocation,
                    onResetFilters = onResetFilters
                )
            }
        } else {
            // Lazy items observing Room database results
            items(
                items = properties,
                key = { it.id }
            ) { property ->
                PropertyCard(
                    property = property,
                    onClick = { onPropertyClick(property) },
                    onToggleSave = { onToggleSave(property) },
                    onContactSeller = { onContactSeller(property) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .testTag("property_item_${property.id}")
                )
            }
        }
    }
}
}

/**
 * Loading State view with fluid Cyber-Luxe skeleton cards when fetching or filtering data.
 */
@Composable
fun PropertyListLoadingState(
    modifier: Modifier = Modifier
) {
    SkeletonPropertyList(
        count = 3,
        modifier = modifier.testTag("property_list_loading_container"),
        showHeaderInfo = true
    )
}

/**
 * Empty State view with actionable reset buttons when no listings match filters.
 */
@Composable
fun PropertyListEmptyState(
    searchQuery: String,
    selectedLocation: String,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("property_list_empty_state"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAltOff,
                    contentDescription = "No properties found",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Properties Found",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            val subtitleText = when {
                searchQuery.isNotBlank() -> "No properties matched \"$searchQuery\" in $selectedLocation."
                else -> "There are no listings matching your current price or category filters."
            }

            Text(
                text = subtitleText,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onResetFilters,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("empty_state_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset All Filters",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
