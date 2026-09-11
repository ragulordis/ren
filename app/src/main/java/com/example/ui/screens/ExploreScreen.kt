package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Property
import com.example.ui.components.SellingSpeedBadge
import com.example.ui.components.SkeletonMapView
import com.example.ui.components.getDrawableResForName
import com.example.ui.theme.CardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.viewmodel.QuickNestViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun ExploreScreen(
    viewModel: QuickNestViewModel,
    modifier: Modifier = Modifier
) {
    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val selectedProperty by viewModel.exploreSelectedProperty.collectAsStateWithLifecycle()
    val radiusKm by viewModel.exploreRadiusKm.collectAsStateWithLifecycle()
    val activeFiltersCount by viewModel.activeFiltersCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val mapsApiKey = remember {
        try {
            com.example.BuildConfig.MAPS_API_KEY
        } catch (e: Throwable) {
            ""
        }
    }
    val isKeyValid = remember(mapsApiKey) {
        mapsApiKey.isNotBlank() &&
                !mapsApiKey.equals("YOUR_MAPS_API_KEY", ignoreCase = true) &&
                !mapsApiKey.equals("YOUR_API_KEY", ignoreCase = true) &&
                !mapsApiKey.startsWith("YOUR_", ignoreCase = true) &&
                !mapsApiKey.contains("PLACEHOLDER", ignoreCase = true)
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var useRadarMode by remember(isKeyValid) { mutableStateOf(!isKeyValid) }

    // Center coordinates for Kottakuppam/Pondicherry location hub
    val hubCenter = remember { LatLng(11.9800, 79.8350) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(hubCenter, 13.5f)
    }

    // Animate camera when selectedProperty changes
    LaunchedEffect(selectedProperty) {
        selectedProperty?.let { prop ->
            val pos = LatLng(prop.mapLat, prop.mapLng)
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 15f))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen_container")
    ) {
        if (isKeyValid && !useRadarMode) {
            // Interactive Google Maps SDK view
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = remember(mapType) {
                    MapProperties(
                        mapType = mapType,
                        isMyLocationEnabled = false
                    )
                },
                uiSettings = remember {
                    MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = true,
                        mapToolbarEnabled = false
                    )
                },
                onMapClick = {
                    viewModel.setExploreSelected(null)
                }
            ) {
                // Display markers for filtered properties correlating with active search results
                filteredProperties.forEach { prop ->
                    val markerState = rememberMarkerState(
                        key = "map_marker_${prop.id}",
                        position = LatLng(prop.mapLat, prop.mapLng)
                    )
                    val isSelected = selectedProperty?.id == prop.id

                    MarkerComposable(
                        state = markerState,
                        title = prop.title,
                        snippet = prop.formattedPrice,
                        onClick = {
                            viewModel.setExploreSelected(prop)
                            true
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) UrgencyFlame else when (prop.category.name) {
                                "LAND" -> Color(0xFF059669)
                                "RENT" -> Color(0xFF0284C7)
                                "LEASE" -> Color(0xFF7C3AED)
                                else -> Color(0xFF0F172A)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.5.dp else 1.dp,
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                            ),
                            shadowElevation = if (isSelected) 12.dp else 5.dp,
                            modifier = Modifier.testTag("map_marker_chip_${prop.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = prop.category.iconEmoji,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = prop.formattedPrice,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Stylized Canvas Radar Fallback Mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("radar_canvas_view")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                viewModel.setExploreSelected(null)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    val oceanPath = Path().apply {
                        moveTo(w * 0.82f, 0f)
                        cubicTo(w * 0.78f, h * 0.35f, w * 0.85f, h * 0.7f, w * 0.80f, h)
                        lineTo(w, h)
                        lineTo(w, 0f)
                        close()
                    }
                    drawPath(
                        path = oceanPath,
                        color = Color(0xFF1E3A8A).copy(alpha = 0.45f)
                    )

                    for (i in 1..8) {
                        val y = h * (i / 9f)
                        drawLine(
                            color = Color(0xFF334155).copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    val center = Offset(w * 0.45f, h * 0.48f)
                    val baseRadius = w * 0.22f
                    val multiplier = (radiusKm / 5.0).toFloat().coerceIn(0.6f, 1.6f)

                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                        radius = baseRadius * multiplier * 1.5f,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.3f),
                        radius = baseRadius * multiplier * 1.5f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                }

                val pinOffsets = listOf(
                    Pair(0.48f, 0.36f),
                    Pair(0.28f, 0.65f),
                    Pair(0.68f, 0.40f),
                    Pair(0.24f, 0.25f),
                    Pair(0.52f, 0.52f),
                    Pair(0.35f, 0.72f)
                )

                filteredProperties.take(pinOffsets.size).forEachIndexed { index, prop ->
                    val (xRatio, yRatio) = pinOffsets[index]
                    val isSelected = selectedProperty?.id == prop.id

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = (xRatio * 320).dp, top = (yRatio * 520).dp)
                            .clickable { viewModel.setExploreSelected(prop) }
                            .testTag("radar_pin_${prop.id}")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) UrgencyFlame else when (prop.category.name) {
                                "LAND" -> Color(0xFF059669)
                                "RENT" -> Color(0xFF0284C7)
                                "LEASE" -> Color(0xFF7C3AED)
                                else -> Color(0xFF0F172A)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = prop.category.iconEmoji, fontSize = 12.sp)
                                Text(
                                    text = prop.formattedPrice,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Control Bar Overlay
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 14.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Card with search context & quick filter trigger
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Search: \"$searchQuery\"" else "Interactive Property Map",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${filteredProperties.size} Properties matching filters",
                                    color = Color(0xFFCAC4D0),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Filter Sheet Trigger Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (activeFiltersCount > 0) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2B2830),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF49454F)),
                                modifier = Modifier.clickable { viewModel.openFilterSheet() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filters",
                                        tint = if (activeFiltersCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (activeFiltersCount > 0) "Filters ($activeFiltersCount)" else "Filter",
                                        color = if (activeFiltersCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Refresh / Sync Map Data Button
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { viewModel.refreshData() }
                                    .testTag("refresh_map_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Map Data",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .fillMaxSize()
                                )
                            }

                            // Center Location Fab
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2563EB),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(hubCenter, 13.5f)
                                            )
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Center Map",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }

                    // Map Type Layer Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Map Style:", color = Color(0xFFCAC4D0), fontSize = 11.sp, fontWeight = FontWeight.Medium)

                        // Normal Google Map
                        MapStyleChip(
                            label = "Standard",
                            icon = Icons.Default.Map,
                            isSelected = !useRadarMode && mapType == MapType.NORMAL,
                            onClick = {
                                if (isKeyValid) {
                                    useRadarMode = false
                                    mapType = MapType.NORMAL
                                }
                            }
                        )

                        // Satellite
                        MapStyleChip(
                            label = "Satellite",
                            icon = Icons.Default.Satellite,
                            isSelected = !useRadarMode && mapType == MapType.SATELLITE,
                            onClick = {
                                if (isKeyValid) {
                                    useRadarMode = false
                                    mapType = MapType.SATELLITE
                                }
                            }
                        )

                        // Terrain
                        MapStyleChip(
                            label = "Terrain",
                            icon = Icons.Default.Terrain,
                            isSelected = !useRadarMode && mapType == MapType.TERRAIN,
                            onClick = {
                                if (isKeyValid) {
                                    useRadarMode = false
                                    mapType = MapType.TERRAIN
                                }
                            }
                        )

                        // Radar View Mode
                        MapStyleChip(
                            label = "Radar Grid",
                            icon = Icons.Default.Radar,
                            isSelected = useRadarMode,
                            onClick = {
                                useRadarMode = true
                            }
                        )
                    }

                    if (!isKeyValid) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Interactive Radar Map Active. Set MAPS_API_KEY in AI Studio Secrets panel to enable Google Maps tiles.",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.5.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Selected Property Preview Card (Correlated with clicked map marker)
        AnimatedVisibility(
            visible = selectedProperty != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedProperty?.let { prop ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_property_preview")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Property Thumbnail
                            Image(
                                painter = painterResource(id = getDrawableResForName(prop.imageResName)),
                                contentDescription = prop.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )

                            // Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SellingSpeedBadge(speed = prop.sellingSpeed)
                                    IconButton(
                                        onClick = { viewModel.setExploreSelected(null) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Close preview",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = prop.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = prop.formattedPrice,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${prop.distanceKm} km away",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: View Details & QuickMatch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openQuickMatch(prop) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Icon(Icons.Default.ElectricBolt, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QuickMatch", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.openPropertyDetails(prop) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Details", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen Cyber-Luxe Skeleton Map View while fetching or syncing from Firestore
        AnimatedVisibility(
            visible = (isLoading && filteredProperties.isEmpty()) || isRefreshing,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(250)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(350)),
            modifier = Modifier.fillMaxSize()
        ) {
            SkeletonMapView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_skeleton_map_view")
            )
        }
    }
}

@Composable
private fun MapStyleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2B2830),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF49454F)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFFCAC4D0),
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFE6E1E5)
            )
        }
    }
}
