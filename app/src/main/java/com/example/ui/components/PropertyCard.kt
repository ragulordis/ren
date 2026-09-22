package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.ui.theme.*

@Composable
fun getDrawableResForName(name: String): Int {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.prop_house_kottakuppam
}

/**
 * Ren Luxury Property Card.
 * Structure:
 * Property image -> Listing badge -> Favorite button -> Property title -> Price -> Location -> Key attributes
 */
@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onContactSeller: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.982f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardPressScale_${property.id}"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, RenBorder, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag("property_card_${property.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 1.dp else 2.dp)
    ) {
        Column {
            // 1. Dominant Photography Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                PropertyImage(
                    imageSource = property.imageResName,
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                )

                // Cinematic subtle vignette gradient for text contrast
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                )

                // Top Bar: Listing Badge & Spring-Animated Favorite Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Listing Badge
                    when {
                        property.sellingSpeed == SellingSpeed.FAST || property.sellingSpeed == SellingSpeed.URGENT -> {
                            RenBadge(
                                text = "⚡ FAST SALE",
                                containerColor = RenDeepNavy.copy(alpha = 0.9f),
                                contentColor = RenGold,
                                borderColor = RenGold.copy(alpha = 0.5f)
                            )
                        }
                        property.category == PropertyCategory.RENT || property.listingType == ListingType.RENT -> {
                            RenBadge(
                                text = "FOR RENT",
                                containerColor = RenPrimaryNavy.copy(alpha = 0.88f),
                                contentColor = RenSurfaceWhite
                            )
                        }
                        property.category == PropertyCategory.LAND -> {
                            RenBadge(
                                text = "LAND FOR SALE",
                                containerColor = RenPrimaryNavy.copy(alpha = 0.88f),
                                contentColor = RenSurfaceWhite
                            )
                        }
                        else -> {
                            RenBadge(
                                text = "FOR SALE",
                                containerColor = RenPrimaryNavy.copy(alpha = 0.88f),
                                contentColor = RenSurfaceWhite
                            )
                        }
                    }

                    // Favorite Button with tactile spring bounce
                    val heartScale by animateFloatAsState(
                        targetValue = if (property.isSaved) 1.2f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "heartScale_${property.id}"
                    )

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .size(38.dp)
                            .background(RenSurfaceWhite.copy(alpha = 0.92f), CircleShape)
                            .border(1.dp, RenBorder.copy(alpha = 0.6f), CircleShape)
                            .testTag("save_button_${property.id}")
                            .testTag("favorite_button_${property.id}")
                    ) {
                        Icon(
                            imageVector = if (property.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (property.isSaved) "Remove from favorites" else "Add to favorites",
                            tint = if (property.isSaved) RenError else RenPrimaryNavy,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(heartScale)
                        )
                    }
                }

                // Verified pill on bottom-right of image if verified
                if (property.verificationLevel > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RenSurfaceWhite.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, RenBorder),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = RenSuccess, modifier = Modifier.size(12.dp))
                            Text(
                                text = if (property.verificationLevel >= 3) "Ownership Verified" else "Verified",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RenPrimaryNavy
                            )
                        }
                    }
                }
            }

            // 2. Card Content & Metadata
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title
                Text(
                    text = property.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Price Hierarchy (Prominent numeric price)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RenPriceText(
                        formattedPrice = property.formattedPrice,
                        priceFontSize = 20.sp,
                        periodFontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (property.marketEstimate > property.price) {
                        Text(
                            text = property.formattedMarketEstimate,
                            fontSize = 12.sp,
                            color = RenTextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Location with pin
                RenLocationRow(
                    location = "${property.location} • ${property.distanceKm} km away"
                )

                // Key Attributes Matrix
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RenSecondaryIvory)
                        .border(1.dp, RenBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (property.category == PropertyCategory.LAND) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.SquareFoot, null, tint = RenPrimaryNavy, modifier = Modifier.size(14.dp))
                            Text("${property.areaSqFt} sq.ft", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RenPrimaryNavy)
                        }
                        Text(
                            text = property.propertyType,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = RenTextSecondary
                        )
                    } else {
                        if (property.bedrooms > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Bed, null, tint = RenPrimaryNavy, modifier = Modifier.size(14.dp))
                                Text("${property.bedrooms} Beds", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RenPrimaryNavy)
                            }
                        }
                        if (property.bathrooms > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Bathtub, null, tint = RenPrimaryNavy, modifier = Modifier.size(14.dp))
                                Text("${property.bathrooms} Baths", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RenPrimaryNavy)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.SquareFoot, null, tint = RenPrimaryNavy, modifier = Modifier.size(14.dp))
                            Text("${property.areaSqFt} sqft", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = RenPrimaryNavy)
                        }
                    }
                }

                // Contact Action
                RenOutlinedButton(
                    text = "Contact Owner",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    onClick = onContactSeller,
                    modifier = Modifier.fillMaxWidth(),
                    height = 42.dp,
                    testTag = "contact_seller_button_${property.id}"
                )
            }
        }
    }
}

/**
 * Fast Sale Card with refined luxury urgency.
 * Uses deep navy background and champagne gold accents without loud colors.
 */
@Composable
fun UrgentPropertyCard(
    property: Property,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onContactSeller: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "urgentCardPressScale_${property.id}"
    )

    Card(
        modifier = modifier
            .width(290.dp)
            .scale(cardScale)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, RenGold.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag("urgent_card_${property.id}"),
        colors = CardDefaults.cardColors(containerColor = RenDeepNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Fast Sale Badge & Expiry + Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RenBadge(
                    text = "⚡ FAST SALE",
                    containerColor = RenGold.copy(alpha = 0.15f),
                    contentColor = RenGold,
                    borderColor = RenGold.copy(alpha = 0.5f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "48 hrs left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RenGold
                    )

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (property.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (property.isSaved) RenError else RenSurfaceWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Title & Location
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = property.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RenSurfaceWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Near ${property.location}",
                    fontSize = 12.sp,
                    color = RenDarkTextSecondary
                )
            }

            // Price & Thumbnail Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(RenDarkSurface)
                    .border(1.dp, RenDarkBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OFFER PRICE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = RenGold
                    )
                    Text(
                        text = property.formattedPrice,
                        color = RenSurfaceWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, RenDarkBorder, RoundedCornerShape(8.dp))
                ) {
                    PropertyImage(
                        imageSource = property.imageResName,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Quick Contact Button
            RenOutlinedButton(
                text = "Contact Seller",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = onContactSeller,
                borderColor = RenGold.copy(alpha = 0.6f),
                textColor = RenGold,
                modifier = Modifier.fillMaxWidth(),
                height = 38.dp,
                testTag = "urgent_contact_seller_button_${property.id}"
            )
        }
    }
}

@Composable
fun SellingSpeedBadge(speed: SellingSpeed) {
    when (speed) {
        SellingSpeed.FAST, SellingSpeed.URGENT -> RenBadge(
            text = "⚡ FAST SALE",
            containerColor = RenDeepNavy,
            contentColor = RenGold,
            borderColor = RenGold.copy(alpha = 0.4f)
        )
        SellingSpeed.PRIVATE -> RenBadge(
            text = "EXCLUSIVE",
            containerColor = RenPrimaryNavy,
            contentColor = RenSurfaceWhite
        )
        SellingSpeed.NORMAL -> RenBadge(
            text = "VERIFIED",
            containerColor = RenSuccessSurface,
            contentColor = RenSuccess,
            borderColor = RenSuccess.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun VerificationBadge(level: Int) {
    if (level > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = RenSuccess, modifier = Modifier.size(13.dp))
            Text(
                text = if (level >= 3) "Ownership Verified" else "Verified",
                fontSize = 11.sp,
                color = RenSuccess,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UrgencyScoreRow(score: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(Icons.Default.Bolt, null, tint = RenGold, modifier = Modifier.size(13.dp))
        Text(text = "$score%", fontSize = 11.sp, color = RenPrimaryNavy, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OpportunityPill(savingsText: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = RenGoldSurface,
        border = BorderStroke(1.dp, RenGoldBorder)
    ) {
        Text(
            text = savingsText,
            color = RenPrimaryNavy,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
