package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Property
import com.example.data.model.SellingSpeed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FastSaleAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.UrgencyFlameContainer
import com.example.ui.theme.VerifiedGreen
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Comprehensive Property Details Modal Sheet.
 * Displays deep real estate insights, photo header, loan calculator,
 * neighborhood points of interest, verified legal credentials, and direct contact actions.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailSheet(
    property: Property,
    onDismiss: () -> Unit,
    onToggleSave: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenVisitBooking: () -> Unit,
    onOpenQuickMatch: () -> Unit,
    onOpenReport: (() -> Unit)? = null,
    onBlockOwner: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var showEmiCalculator by remember { mutableStateOf(false) }
    var loanTenureYears by remember { mutableIntStateOf(20) }
    var downPaymentPercent by remember { mutableFloatStateOf(20f) }

    // Calculate approximate price per sq.ft
    val pricePerSqFt = remember(property.price, property.areaSqFt) {
        if (property.areaSqFt > 0) property.price / property.areaSqFt else 0L
    }

    // EMI calculation: P * r * (1+r)^n / ((1+r)^n - 1)
    val calculatedEmi = remember(property.price, downPaymentPercent, loanTenureYears) {
        val loanAmount = property.price * (1f - (downPaymentPercent / 100f))
        val monthlyRate = 0.085 / 12.0 // standard 8.5% p.a.
        val totalMonths = loanTenureYears * 12
        if (loanAmount <= 0) 0L
        else {
            val emi = (loanAmount * monthlyRate * (1.0 + monthlyRate).pow(totalMonths.toDouble())) /
                    ((1.0 + monthlyRate).pow(totalMonths.toDouble()) - 1.0)
            emi.roundToLong()
        }
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 44.dp, height = 4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("property_detail_sheet")
        ) {
            // 1. Hero Image Container with Actions and Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                PropertyImage(
                    imageSource = property.imageResName,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                // High-contrast Gradient Vignette
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top Floating Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .testTag("property_detail_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share Action
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "Property in ${property.location}: ${property.title}"
                                    )
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this property on Ren:\n${property.title}\nPrice: ${property.formattedPrice}\nLocation: ${property.location}\nArea: ${property.areaSqFt} sq.ft"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Property"))
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("property_detail_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }

                        if (onOpenReport != null) {
                            IconButton(
                                onClick = onOpenReport,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .testTag("property_detail_report_button")
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = "Report Listing", tint = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        IconButton(
                            onClick = onOpenQuickMatch,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("property_detail_quickmatch_button")
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = "QuickMatch", tint = FastSaleAmber)
                        }

                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .testTag("property_detail_save_button")
                        ) {
                            Icon(
                                imageVector = if (property.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Save",
                                tint = if (property.isSaved) UrgencyFlame else Color.White
                            )
                        }
                    }
                }

                // Bottom Overlay Status & Photo Counter
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SellingSpeedBadge(speed = property.sellingSpeed)
                        VerificationBadge(level = property.verificationLevel)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(
                                text = "${property.imageUrls.ifEmpty { listOf(property.imageResName) }.size} photo(s)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 2. Main Sheet Body
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (property.imageUrls.size > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Property gallery", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            property.imageUrls.take(4).forEach { imageUrl ->
                                PropertyImage(
                                    imageSource = imageUrl,
                                    contentDescription = "${property.title} gallery image",
                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
                                )
                            }
                        }

                        if (onBlockOwner != null) {
                            IconButton(
                                onClick = onBlockOwner,
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.6f), CircleShape).testTag("property_detail_block_owner_button")
                            ) {
                                Icon(Icons.Default.PersonOff, contentDescription = "Block user", tint = Color.White.copy(alpha = 0.85f))
                            }
                        }
                    }
                }

                // Price & Value Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = property.formattedPrice,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("property_detail_price")
                        )

                        if (property.marketEstimate > property.price) {
                            Text(
                                text = property.formattedMarketEstimate,
                                fontSize = 15.sp,
                                color = TextSecondary,
                                textDecoration = TextDecoration.LineThrough
                            )
                            OpportunityPill(savingsText = property.formattedSavings)
                        }
                    }

                    // Key Metrics Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pricePerSqFt > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "₹$pricePerSqFt / sq.ft",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showEmiCalculator = !showEmiCalculator }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Calculate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "EMI from ₹${calculatedEmi.takeIf { it > 0 } ?: 18500}/mo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Urgency:", fontSize = 11.sp, color = TextSecondary)
                            UrgencyScoreRow(score = property.urgencyScore)
                        }
                    }
                }

                // Interactive EMI Calculator Expansion
                AnimatedVisibility(visible = showEmiCalculator) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Calculate, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Interactive Home Loan EMI Estimator",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Text(
                                    text = "@ 8.5% p.a.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Estimated Monthly EMI", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = "₹$calculatedEmi",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Down Payment (${downPaymentPercent.toInt()}%)", fontSize = 11.sp, color = TextSecondary)
                                    val downPaymentAmt = (property.price * (downPaymentPercent / 100f)).toLong()
                                    Text(
                                        text = "₹$downPaymentAmt",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            // Down Payment Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Down Payment", fontSize = 11.sp, color = TextSecondary)
                                    Text("${downPaymentPercent.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Slider(
                                    value = downPaymentPercent,
                                    onValueChange = { downPaymentPercent = it },
                                    valueRange = 10f..50f,
                                    steps = 7,
                                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                                )
                            }

                            // Loan Tenure Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tenure:", fontSize = 11.sp, color = TextSecondary)
                                listOf(10, 15, 20, 25).forEach { yrs ->
                                    FilterChip(
                                        selected = loanTenureYears == yrs,
                                        onClick = { loanTenureYears = yrs },
                                        label = { Text("${yrs}Y", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Title & Location with Navigation Direct Trigger
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = property.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.testTag("property_detail_title")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "${property.approximateArea} (${property.distanceKm} km from center)",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("property_detail_location")
                            )
                        }

                        // Map navigation intent trigger
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.clickable {
                                try {
                                    val geoUri = Uri.parse("geo:${property.mapLat},${property.mapLng}?q=${property.mapLat},${property.mapLng}(${Uri.encode(property.title)})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Ignore if no map app
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Directions, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Specs Matrix (Beds, Baths, SqFt, Type, Floor, Direction)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (property.bedrooms > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("${property.bedrooms} BHK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Bedrooms", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                    if (property.bathrooms > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Bathtub, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(3.dp))
                            Text("${property.bathrooms} Baths", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Baths", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SquareFoot, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("${property.areaSqFt} sq.ft", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Area", fontSize = 10.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(property.category.iconEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(property.propertyType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Type", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                // Property Description Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.testTag("property_detail_description_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Property Overview & Description",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        val descriptionText = property.description
                        val maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3

                        Text(
                            text = descriptionText,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp,
                            maxLines = maxLines,
                            modifier = Modifier.testTag("property_detail_description_text")
                        )

                        if (descriptionText.length > 100) {
                            Row(
                                modifier = Modifier
                                    .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isDescriptionExpanded) "Show Less" else "Read Full Description",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = if (isDescriptionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Neighborhood Highlights & Proximity
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PinDrop, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Neighborhood & Key Landmarks",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Serenity Beach: 1.2 km", fontSize = 11.5.sp, color = TextSecondary)
                                Text("Health Center: 900 m", fontSize = 11.5.sp, color = TextSecondary)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Auroville School: 1.8 km", fontSize = 11.5.sp, color = TextSecondary)
                                Text("Local Market: 400 m", fontSize = 11.5.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                // Amenities & Features List
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("property_detail_amenities_section")
                ) {
                    Text(
                        text = "Amenities & Key Features",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        property.featuresList.forEach { feat ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = feat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Price History & Valuation Intelligence Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = UrgencyFlame, modifier = Modifier.size(16.dp))
                            Text("Price Intelligence & Discounts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(
                            text = "• Market Valuation: ${property.formattedMarketEstimate}\n• Original Ask: ${property.formattedOriginalPrice}\n• Current Offer: ${property.formattedPrice} (Direct seller pricing)",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Legal Due Diligence Badges
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Verified, null, tint = VerifiedGreen, modifier = Modifier.size(16.dp))
                            Text("Legal Due Diligence Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("✓ Title Deed Verified", fontSize = 11.5.sp, color = VerifiedGreen, fontWeight = FontWeight.SemiBold)
                                Text("✓ 15-Year EC Clear", fontSize = 11.5.sp, color = VerifiedGreen, fontWeight = FontWeight.SemiBold)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("✓ RERA Approved", fontSize = 11.5.sp, color = VerifiedGreen, fontWeight = FontWeight.SemiBold)
                                Text("✓ Zero Brokerage Option", fontSize = 11.5.sp, color = VerifiedGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Interactive Location & Map Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("property_detail_map_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Location & Neighborhood",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "${property.distanceKm} km from Hub",
                                fontSize = 11.5.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "${property.approximateArea}, ${property.location}",
                            fontSize = 12.5.sp,
                            color = TextSecondary
                        )

                        // Google Map Preview
                        val propLocation = remember(property.mapLat, property.mapLng) {
                            LatLng(property.mapLat, property.mapLng)
                        }
                        val mapCameraState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(propLocation, 15f)
                        }
                        val markerState = rememberMarkerState(position = propLocation)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        ) {
                            if (isKeyValid) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = mapCameraState,
                                    properties = remember { MapProperties(isMyLocationEnabled = false) },
                                    uiSettings = remember {
                                        MapUiSettings(
                                            zoomControlsEnabled = false,
                                            compassEnabled = true,
                                            mapToolbarEnabled = false,
                                            scrollGesturesEnabled = true,
                                            zoomGesturesEnabled = true
                                        )
                                    }
                                ) {
                                    Marker(
                                        state = markerState,
                                        title = property.title,
                                        snippet = property.formattedPrice
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                        Text(property.approximateArea, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${property.mapLat}, ${property.mapLng}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        // Navigation Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val geoUri = Uri.parse("geo:${property.mapLat},${property.mapLng}?q=${property.mapLat},${property.mapLng}(${Uri.encode(property.title)})")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Directions, null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Directions", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val gmmIntentUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${property.mapLat},${property.mapLng}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Maps", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Agent & Owner Contact Info Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("property_detail_agent_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = property.ownerName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = property.ownerName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified Agent",
                                        tint = VerifiedGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Text(
                                    text = "${property.ownerType} • Active in ${property.location}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VerifiedGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Responds quickly",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VerifiedGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder)

                        // Public listings use Ren Chat; private contact details are not exposed.
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Use Ren Chat to contact the owner. Contact details remain private until the owner chooses to share them.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                // Sticky Bottom Action Controls (Chat & Schedule Visit)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenChat,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("property_detail_chat_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Chat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenVisitBooking,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("property_detail_visit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Schedule Visit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
