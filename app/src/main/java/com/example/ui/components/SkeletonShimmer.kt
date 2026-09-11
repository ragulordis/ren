package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle

/**
 * Creates a fluid cyber-luxe shimmer brush that sweeps across UI elements.
 * Uses high-contrast neon tints in dark theme and crisp slate accents in light theme.
 */
@Composable
fun rememberShimmerBrush(
    targetValue: Float = 1400f,
    durationMillis: Int = 1300
): Brush {
    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1E293B).copy(alpha = 0.85f),
            Color(0xFF334155).copy(alpha = 0.95f),
            Color(0xFF38BDF8).copy(alpha = 0.35f), // Electric Cyan cyber accent
            Color(0xFF334155).copy(alpha = 0.95f),
            Color(0xFF1E293B).copy(alpha = 0.85f)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFF0284C7).copy(alpha = 0.22f), // Cyan highlight
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 350f, y = translateAnim - 350f),
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Generic skeleton placeholder box with rounded corners and animated shimmer brush.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    brush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

/**
 * High-fidelity Skeleton loader for PropertyCard matching exact dimensions,
 * badges, image container, and bottom action items.
 */
@Composable
fun SkeletonPropertyCard(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .testTag("skeleton_property_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image Box placeholder with overlaid badge skeletons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .background(brush)
            ) {
                // Top badges row (Urgency badge on left, heart favorite icon on right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(115.dp)
                            .height(26.dp),
                        shape = RoundedCornerShape(14.dp),
                        brush = brush
                    )

                    SkeletonBox(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        brush = brush
                    )
                }

                // Bottom image overlay (Price and savings tag placeholders)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(110.dp)
                                .height(24.dp),
                            shape = RoundedCornerShape(6.dp),
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(75.dp)
                                .height(18.dp),
                            shape = RoundedCornerShape(10.dp),
                            brush = brush
                        )
                    }
                }
            }

            // Card Body Details
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title line
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(18.dp),
                    shape = RoundedCornerShape(6.dp),
                    brush = brush
                )

                // Location line & Verification pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SkeletonBox(
                            modifier = Modifier.size(16.dp),
                            shape = CircleShape,
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(140.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                    }

                    SkeletonBox(
                        modifier = Modifier
                            .width(68.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(10.dp),
                        brush = brush
                    )
                }

                // Specs: BHK, Bath, SqFt container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(56.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(56.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(70.dp)
                                .height(14.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width(50.dp)
                                .height(16.dp),
                            shape = RoundedCornerShape(8.dp),
                            brush = brush
                        )
                    }
                }

                // Action Row skeleton (Contact seller button)
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    brush = brush
                )
            }
        }
    }
}

/**
 * Skeleton placeholder for Urgent Deals horizontal card.
 */
@Composable
fun SkeletonUrgentPropertyCard(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    Surface(
        modifier = modifier
            .width(290.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
            .testTag("skeleton_urgent_property_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Urgency Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(modifier = Modifier.width(85.dp).height(20.dp), shape = RoundedCornerShape(10.dp), brush = brush)
                SkeletonBox(modifier = Modifier.width(105.dp).height(20.dp), shape = RoundedCornerShape(10.dp), brush = brush)
            }

            // Title line
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.85f).height(16.dp), shape = RoundedCornerShape(4.dp), brush = brush)

            // Location line
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp), shape = RoundedCornerShape(4.dp), brush = brush)

            // Price & Thumbnail Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SkeletonBox(modifier = Modifier.width(60.dp).height(10.dp), shape = RoundedCornerShape(3.dp), brush = brush)
                    SkeletonBox(modifier = Modifier.width(90.dp).height(18.dp), shape = RoundedCornerShape(4.dp), brush = brush)
                }

                SkeletonBox(
                    modifier = Modifier
                        .size(width = 64.dp, height = 48.dp),
                    shape = RoundedCornerShape(10.dp),
                    brush = brush
                )
            }

            // Button skeleton
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(36.dp), shape = RoundedCornerShape(10.dp), brush = brush)
        }
    }
}

/**
 * Vertical list of skeleton property cards with informative status header.
 */
@Composable
fun SkeletonPropertyList(
    count: Int = 3,
    modifier: Modifier = Modifier,
    showHeaderInfo: Boolean = true
) {
    val brush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skeleton_property_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showHeaderInfo) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("skeleton_sync_status_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Syncing from Firestore",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Syncing with Cloud Firestore...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Fetching verified properties across Kottakuppam & Pondicherry",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        repeat(count) { index ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                SkeletonPropertyCard(
                    modifier = Modifier.testTag("skeleton_property_card_$index"),
                    brush = brush
                )
            }
        }
    }
}

/**
 * Immersive Cyber-Luxe Skeleton Map View with pulsing radar sweeps,
 * coordinate gridlines, shimmering map pins, top filter controls, and bottom preview card.
 */
@Composable
fun SkeletonMapView(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush(targetValue = 1800f, durationMillis = 1400)
    val infiniteTransition = rememberInfiniteTransition(label = "mapRadarTransition")

    // Pulsing radar ripple ring animation
    val pulseRadius1 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 160f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha1"
    )

    val pulseRadius2 by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .testTag("skeleton_map_view")
    ) {
        // 1. Interactive canvas gridlines & pulsing radar hubs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background subtle gridlines simulating latitude & longitude map coordinates
            val gridStep = 45.dp.toPx()
            var x = 0f
            while (x < w) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.45f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += gridStep
            }

            var y = 0f
            while (y < h) {
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.45f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Coastline curve representation on East side (Bay of Bengal)
            val coastPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.84f, 0f)
                cubicTo(w * 0.78f, h * 0.35f, w * 0.86f, h * 0.7f, w * 0.81f, h)
                lineTo(w, h)
                lineTo(w, 0f)
                close()
            }
            drawPath(
                path = coastPath,
                color = Color(0xFF0369A1).copy(alpha = 0.25f)
            )

            // Radar hub center near Kottakuppam/Pondicherry
            val hubCenter = Offset(w * 0.48f, h * 0.42f)

            // Expanding radar sonar waves
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = pulseAlpha1),
                radius = pulseRadius1.dp.toPx(),
                center = hubCenter,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = pulseAlpha2),
                radius = pulseRadius2.dp.toPx(),
                center = hubCenter,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Center beacon glow
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = 0.8f),
                radius = 7.dp.toPx(),
                center = hubCenter
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = hubCenter
            )
        }

        // 2. Simulated Shimmering Map Marker Pins across the region
        val mockMarkerCoordinates = listOf(
            Pair(0.24f, 0.28f), // Auroville Road
            Pair(0.48f, 0.39f), // Kottakuppam Main
            Pair(0.68f, 0.32f), // Serenity Beach
            Pair(0.32f, 0.62f), // Pondicherry Town
            Pair(0.60f, 0.54f), // White Town
            Pair(0.20f, 0.76f)  // Villianur Road
        )

        mockMarkerCoordinates.forEachIndexed { index, (xRatio, yRatio) ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (340 * xRatio).dp,
                        y = (560 * yRatio).dp
                    )
                    .testTag("skeleton_map_marker_$index")
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (index == 1) Color(0xFF38BDF8) else Color(0xFF475569)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        SkeletonBox(
                            modifier = Modifier.size(12.dp),
                            shape = CircleShape,
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier
                                .width((42 + (index % 3) * 10).dp)
                                .height(12.dp),
                            shape = RoundedCornerShape(4.dp),
                            brush = brush
                        )
                    }
                }
            }
        }

        // 3. Center Floating Live Cloud Sync Pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.92f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp)
                .testTag("skeleton_map_sync_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Loading Firestore Map Coordinates...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Top Map Header Skeleton Overlay
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                .testTag("skeleton_map_header")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        SkeletonBox(
                            modifier = Modifier.size(20.dp),
                            shape = CircleShape,
                            brush = brush
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SkeletonBox(
                                modifier = Modifier.width(160.dp).height(14.dp),
                                shape = RoundedCornerShape(4.dp),
                                brush = brush
                            )
                            SkeletonBox(
                                modifier = Modifier.width(110.dp).height(11.dp),
                                shape = RoundedCornerShape(4.dp),
                                brush = brush
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SkeletonBox(
                            modifier = Modifier.width(65.dp).height(28.dp),
                            shape = RoundedCornerShape(12.dp),
                            brush = brush
                        )
                        SkeletonBox(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            brush = brush
                        )
                    }
                }

                // Map style chips skeleton
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        SkeletonBox(
                            modifier = Modifier.width(58.dp).height(24.dp),
                            shape = RoundedCornerShape(12.dp),
                            brush = brush
                        )
                    }
                }
            }
        }

        // 5. Bottom Preview Card Skeleton
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
            shadowElevation = 14.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 20.dp)
                .testTag("skeleton_map_bottom_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .size(width = 80.dp, height = 70.dp),
                    shape = RoundedCornerShape(14.dp),
                    brush = brush
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(
                        modifier = Modifier.width(140.dp).height(16.dp),
                        shape = RoundedCornerShape(4.dp),
                        brush = brush
                    )
                    SkeletonBox(
                        modifier = Modifier.width(90.dp).height(14.dp),
                        shape = RoundedCornerShape(4.dp),
                        brush = brush
                    )
                    SkeletonBox(
                        modifier = Modifier.width(110.dp).height(12.dp),
                        shape = RoundedCornerShape(4.dp),
                        brush = brush
                    )
                }

                SkeletonBox(
                    modifier = Modifier.size(width = 38.dp, height = 38.dp),
                    shape = CircleShape,
                    brush = brush
                )
            }
        }
    }
}
