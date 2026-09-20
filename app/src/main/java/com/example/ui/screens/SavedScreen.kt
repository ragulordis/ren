package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.ui.components.PropertyCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FastSaleAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.UrgencyFlameContainer
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenContainer
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.QuickNestViewModel
import com.example.viewmodel.SavedViewModel

@Composable
fun SavedScreen(
    savedViewModel: SavedViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val savedProperties by savedViewModel.savedProperties.collectAsStateWithLifecycle()
    val visits by savedViewModel.visits.collectAsStateWithLifecycle()

    SavedScreen(
        savedProperties = savedProperties,
        visits = visits,
        onOpenPropertyDetails = { mainViewModel.openPropertyDetails(it) },
        onToggleSave = { savedViewModel.toggleSave(it) },
        onContactSeller = { mainViewModel.openContactSeller(it) },
        onUpdateVisitStatus = { id, status -> savedViewModel.updateVisitStatus(id, status) },
        onCancelVisit = { savedViewModel.cancelVisit(it) },
        onDeleteVisit = { savedViewModel.deleteVisit(it) },
        modifier = modifier
    )
}

@Composable
fun SavedScreen(
    viewModel: QuickNestViewModel,
    modifier: Modifier = Modifier
) {
    val savedProperties by viewModel.savedProperties.collectAsStateWithLifecycle()
    val visits by viewModel.visits.collectAsStateWithLifecycle()

    SavedScreen(
        savedProperties = savedProperties,
        visits = visits,
        onOpenPropertyDetails = { viewModel.openPropertyDetails(it) },
        onToggleSave = { viewModel.toggleSave(it) },
        onContactSeller = { viewModel.openContactSeller(it) },
        onUpdateVisitStatus = { id, status -> viewModel.updateVisitStatus(id, status) },
        onCancelVisit = { viewModel.cancelVisit(it) },
        onDeleteVisit = { viewModel.deleteVisit(it) },
        modifier = modifier
    )
}

@Composable
fun SavedScreen(
    savedProperties: List<Property>,
    visits: List<com.example.data.model.PropertyVisit>,
    onOpenPropertyDetails: (Property) -> Unit,
    onToggleSave: (Property) -> Unit,
    onContactSeller: (Property) -> Unit,
    onUpdateVisitStatus: (String, String) -> Unit,
    onCancelVisit: (String) -> Unit,
    onDeleteVisit: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var selectedTab by remember { mutableStateOf(0) } // 0: Saved Properties, 1: Booked Visits
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredSaved = when (selectedFilter) {
        "Urgent Deals" -> savedProperties.filter { it.sellingSpeed == SellingSpeed.URGENT || it.sellingSpeed == SellingSpeed.FAST }
        "Rentals" -> savedProperties.filter { it.listingType == ListingType.RENT }
        "Lands" -> savedProperties.filter { it.category == PropertyCategory.LAND }
        else -> savedProperties
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("saved_screen_content"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "Saved & Activity",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Track your favorite deals, price reductions, and scheduled visits",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // 2. Smart Notification / Price Drop Alert (Section 15 of Blueprint)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = UrgencyFlameContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, UrgencyFlame.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(UrgencyFlame, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Price drop alert: -₹2,00,000",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = UrgencyFlame
                        )
                        Text(
                            text = "2BHK in Kottakuppam reduced from ₹38L to ₹35L. 4 buyers scheduled visits.",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 3. Tab Switcher (Saved Properties vs Scheduled Visits)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Saved (${savedProperties.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Site visits (${visits.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        // 4. Sub-filters for Saved Properties tab
        if (selectedTab == 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Urgent Deals", "Rentals", "Lands").forEach { filterLabel ->
                        val isSel = selectedFilter == filterLabel
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedFilter = filterLabel },
                            label = { Text(filterLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filteredSaved.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(36.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No saved properties yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tap the heart icon on any property to track it here",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredSaved, key = { it.id }) { property ->
                    PropertyCard(
                        property = property,
                        onClick = { onOpenPropertyDetails(property) },
                        onToggleSave = { onToggleSave(property) },
                        onContactSeller = { onContactSeller(property) }
                    )
                }
            }
        } else {
            // Visits Tab
            if (visits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(36.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No scheduled site visits",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Book a property walkthrough directly from property details",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(visits, key = { it.id }) { visit ->
                    val statusColor = when (visit.status) {
                        "Confirmed" -> VerifiedGreen
                        "Completed" -> MaterialTheme.colorScheme.primary
                        "Cancelled" -> TextSecondary
                        else -> FastSaleAmber
                    }
                    val statusBg = when (visit.status) {
                        "Confirmed" -> VerifiedGreenContainer
                        "Completed" -> MaterialTheme.colorScheme.primaryContainer
                        "Cancelled" -> MaterialTheme.colorScheme.surfaceVariant
                        else -> FastSaleAmber.copy(alpha = 0.12f)
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "${visit.date} at ${visit.timeSlot}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null, tint = statusColor, modifier = Modifier.size(12.dp))
                                        Text(
                                            text = visit.status,
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = visit.propertyTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${visit.location} • Direct owner access confirmed",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            // Interactive Visit Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (visit.status == "Confirmed") {
                                    OutlinedButton(
                                        onClick = { onUpdateVisitStatus(visit.id, "Completed") },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Mark Visited", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = { onCancelVisit(visit.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Cancel Visit", fontSize = 11.sp, color = UrgencyFlame)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onDeleteVisit(visit.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Remove from List", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
