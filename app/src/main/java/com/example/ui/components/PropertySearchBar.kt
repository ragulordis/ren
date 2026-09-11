package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame

/**
 * Material 3 Search Bar Component for Property Listings.
 *
 * Provides Material 3 text fields for:
 * 1. Keyword search (title, property type, tags).
 * 2. Location filtering (text field with dropdown and quick chips).
 * 3. Price range filtering (Material 3 numeric text fields for Min & Max price with quick budget presets).
 *
 * Updates local state and propagates changes to filter properties.
 */
@Composable
fun PropertySearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLocation: String,
    onLocationChange: (String) -> Unit,
    minPrice: Long?,
    maxPrice: Long?,
    onPriceRangeChange: (min: Long?, max: Long?) -> Unit,
    modifier: Modifier = Modifier,
    availableLocations: List<String> = listOf("All Locations", "Kottakuppam", "Pondicherry", "Auroville", "Serenity Beach"),
    onOpenFilterSheet: (() -> Unit)? = null,
    activeFiltersCount: Int = 0,
    onSearchSubmitted: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var locationMenuExpanded by remember { mutableStateOf(false) }

    // Local state for the price range text fields
    var minPriceInput by remember(minPrice) {
        mutableStateOf(minPrice?.toString() ?: "")
    }
    var maxPriceInput by remember(maxPrice) {
        mutableStateOf(maxPrice?.toString() ?: "")
    }

    // Keep local text fields in sync when external price range changes
    LaunchedEffect(minPrice, maxPrice) {
        minPriceInput = minPrice?.toString() ?: ""
        maxPriceInput = maxPrice?.toString() ?: ""
    }

    val focusManager = LocalFocusManager.current
    val hasActivePriceFilter = minPrice != null || maxPrice != null
    val hasActiveLocationFilter = selectedLocation != "All Locations" && selectedLocation.isNotBlank()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("property_search_bar_container"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Primary Search TextField + Price/Filter Expand Toggle + Optional Bottom Sheet Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary Material 3 Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search properties, areas, keywords...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                onSearchSubmitted?.invoke(searchQuery)
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = CardBorderSubtle,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("property_search_input")
                )

                // Toggle Button for Location & Price Range Material 3 Inputs
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isExpanded || hasActivePriceFilter || hasActiveLocationFilter) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .border(
                            1.dp,
                            if (isExpanded || hasActivePriceFilter || hasActiveLocationFilter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                CardBorderSubtle
                            },
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { isExpanded = !isExpanded }
                        .testTag("toggle_price_filter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Toggle location and price filter",
                        tint = if (isExpanded || hasActivePriceFilter || hasActiveLocationFilter) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(22.dp)
                    )

                    if (hasActivePriceFilter || hasActiveLocationFilter) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(10.dp)
                                .background(UrgencyFlame, CircleShape)
                        )
                    }
                }

                // Optional Global Sheet Filter Button
                if (onOpenFilterSheet != null) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (activeFiltersCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.dp,
                                if (activeFiltersCount > 0) MaterialTheme.colorScheme.primary else CardBorderSubtle,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onOpenFilterSheet() }
                            .testTag("open_filter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (activeFiltersCount > 0) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )

                        if (activeFiltersCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                                    .background(UrgencyFlame, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeFiltersCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Status & Shortcut summary chips when collapsed
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Pill
                    FilterChip(
                        selected = hasActiveLocationFilter,
                        onClick = { isExpanded = true },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (hasActiveLocationFilter) selectedLocation else "Location: All",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("search_bar_location_pill")
                    )

                    // Price Range Pill
                    FilterChip(
                        selected = hasActivePriceFilter,
                        onClick = { isExpanded = true },
                        label = {
                            val priceLabel = when {
                                minPrice != null && maxPrice != null -> "${formatPriceCompact(minPrice)} - ${formatPriceCompact(maxPrice)}"
                                minPrice != null -> "Min ${formatPriceCompact(minPrice)}"
                                maxPrice != null -> "Max ${formatPriceCompact(maxPrice)}"
                                else -> "Price Range"
                            }
                            Text(text = priceLabel, fontSize = 11.sp)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("search_bar_price_pill")
                    )

                    // Quick Clear button if either filter active
                    if (hasActivePriceFilter || hasActiveLocationFilter) {
                        Text(
                            text = "Reset",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    onLocationChange("All Locations")
                                    onPriceRangeChange(null, null)
                                }
                                .padding(horizontal = 6.dp)
                                .testTag("search_bar_reset_pill")
                        )
                    }
                }
            }

            // Expandable Section: Location and Price Range Material 3 Inputs
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorderSubtle)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Filter Material 3 Text Field with Dropdown
                    Text(
                        text = "FILTER BY LOCATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedLocation,
                            onValueChange = onLocationChange,
                            placeholder = { Text("Enter or select location", color = TextSecondary, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (selectedLocation.isNotBlank() && selectedLocation != "All Locations") {
                                        IconButton(
                                            onClick = { onLocationChange("All Locations") },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear location", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = { locationMenuExpanded = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select location dropdown")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CardBorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("property_location_input")
                        )

                        DropdownMenu(
                            expanded = locationMenuExpanded,
                            onDismissRequest = { locationMenuExpanded = false }
                        ) {
                            availableLocations.forEach { loc ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = loc,
                                            fontWeight = if (loc == selectedLocation) FontWeight.Bold else FontWeight.Normal,
                                            color = if (loc == selectedLocation) MaterialTheme.colorScheme.primary else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        onLocationChange(loc)
                                        locationMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Location Quick Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableLocations.forEach { loc ->
                            val isSelected = selectedLocation.equals(loc, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onLocationChange(loc) },
                                label = { Text(loc, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("location_chip_${loc.replace(" ", "_")}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Price Range Filter Material 3 Text Fields
                    Text(
                        text = "FILTER BY PRICE RANGE (₹)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Min Price Material 3 OutlinedTextField
                        OutlinedTextField(
                            value = minPriceInput,
                            onValueChange = { input ->
                                val cleaned = input.filter { it.isDigit() }
                                minPriceInput = cleaned
                                val parsed = cleaned.toLongOrNull()
                                onPriceRangeChange(parsed, maxPriceInput.toLongOrNull())
                            },
                            label = { Text("Min Price (₹)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. 1000000", fontSize = 11.sp, color = TextSecondary) },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CardBorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("price_min_input")
                        )

                        Text("to", fontSize = 12.sp, color = TextSecondary)

                        // Max Price Material 3 OutlinedTextField
                        OutlinedTextField(
                            value = maxPriceInput,
                            onValueChange = { input ->
                                val cleaned = input.filter { it.isDigit() }
                                maxPriceInput = cleaned
                                val parsed = cleaned.toLongOrNull()
                                onPriceRangeChange(minPriceInput.toLongOrNull(), parsed)
                            },
                            label = { Text("Max Price (₹)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. 5000000", fontSize = 11.sp, color = TextSecondary) },
                            prefix = { Text("₹ ", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = CardBorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("price_max_input")
                        )
                    }

                    // Helper price reading text
                    if (minPriceInput.isNotEmpty() || maxPriceInput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val minVal = minPriceInput.toLongOrNull()
                            val maxVal = maxPriceInput.toLongOrNull()
                            Text(
                                text = if (minVal != null) "From: ${formatPriceDetailed(minVal)}" else "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (maxVal != null) "Up to: ${formatPriceDetailed(maxVal)}" else "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Quick Budget Preset Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Quick Presets:", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))

                    val presets = listOf(
                        Triple("Under ₹20L", 0L, 2_000_000L),
                        Triple("₹20L - ₹50L", 2_000_000L, 5_000_000L),
                        Triple("₹50L - ₹1Cr", 5_000_000L, 10_000_000L),
                        Triple("₹1Cr - ₹3Cr", 10_000_000L, 30_000_000L),
                        Triple("Above ₹3Cr", 30_000_000L, 100_000_000L)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (label, min, max) ->
                            val isSelected = minPrice == min && maxPrice == max
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        minPriceInput = ""
                                        maxPriceInput = ""
                                        onPriceRangeChange(null, null)
                                    } else {
                                        minPriceInput = min.toString()
                                        maxPriceInput = max.toString()
                                        onPriceRangeChange(min, max)
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("quick_budget_chip_${label.replace(" ", "_")}")
                            )
                        }
                    }

                    // Price Action Buttons (Clear & Done)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                minPriceInput = ""
                                maxPriceInput = ""
                                onPriceRangeChange(null, null)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_price_filter_button")
                        ) {
                            Text("Clear Price", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val min = minPriceInput.toLongOrNull()
                                val max = maxPriceInput.toLongOrNull()
                                onPriceRangeChange(min, max)
                                focusManager.clearFocus()
                                isExpanded = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("apply_price_filter_button")
                        ) {
                            Text("Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stateful variant of the PropertySearchBar that encapsulates local filtering state
 * and automatically triggers filter updates.
 */
@Composable
fun PropertySearchBarStateful(
    initialQuery: String = "",
    initialLocation: String = "All Locations",
    initialMinPrice: Long? = null,
    initialMaxPrice: Long? = null,
    onFilterUpdated: (query: String, location: String, minPrice: Long?, maxPrice: Long?) -> Unit,
    modifier: Modifier = Modifier,
    availableLocations: List<String> = listOf("All Locations", "Kottakuppam", "Pondicherry", "Auroville", "Serenity Beach"),
    onOpenFilterSheet: (() -> Unit)? = null,
    activeFiltersCount: Int = 0
) {
    var query by remember { mutableStateOf(initialQuery) }
    var location by remember { mutableStateOf(initialLocation) }
    var minPrice by remember { mutableStateOf(initialMinPrice) }
    var maxPrice by remember { mutableStateOf(initialMaxPrice) }

    PropertySearchBar(
        searchQuery = query,
        onSearchQueryChange = { newQuery ->
            query = newQuery
            onFilterUpdated(query, location, minPrice, maxPrice)
        },
        selectedLocation = location,
        onLocationChange = { newLoc ->
            location = newLoc
            onFilterUpdated(query, location, minPrice, maxPrice)
        },
        minPrice = minPrice,
        maxPrice = maxPrice,
        onPriceRangeChange = { newMin, newMax ->
            minPrice = newMin
            maxPrice = newMax
            onFilterUpdated(query, location, minPrice, maxPrice)
        },
        modifier = modifier,
        availableLocations = availableLocations,
        onOpenFilterSheet = onOpenFilterSheet,
        activeFiltersCount = activeFiltersCount
    )
}

private fun formatPriceCompact(price: Long): String {
    return when {
        price >= 10_000_000 -> "₹${price / 10_000_000.0}Cr".replace(".0Cr", "Cr")
        price >= 100_000 -> "₹${price / 100_000.0}L".replace(".0L", "L")
        price >= 1_000 -> "₹${price / 1000}k"
        else -> "₹$price"
    }
}

private fun formatPriceDetailed(price: Long): String {
    return when {
        price >= 10_000_000 -> "₹%.2f Crores".format(price / 10_000_000.0)
        price >= 100_000 -> "₹%.2f Lakhs".format(price / 100_000.0)
        price >= 1_000 -> "₹%,d".format(price)
        else -> "₹$price"
    }
}
