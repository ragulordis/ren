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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Property
import com.example.data.model.SellingSpeed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CoolDarkCardGradient
import com.example.ui.theme.CoolGlassmorphicBorder
import com.example.ui.theme.CoolHeroGradient
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyDarkBorder
import com.example.ui.theme.UrgencyDarkCard
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.BlueCorporate
import com.example.ui.theme.AccentGold

@Composable
fun getDrawableResForName(name: String): Int {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resId != 0) resId else R.drawable.prop_house_kottakuppam
}

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
        targetValue = if (isPressed) 0.978f else 1.0f,
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
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag("property_card_${property.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 1.dp else 3.dp)
    ) {
        Column {
            // Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
            ) {
                Image(
                    painter = painterResource(id = getDrawableResForName(property.imageResName)),
                    contentDescription = property.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                )

                // Gradient overlay at top and bottom for high readability & cool ambiance
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Top Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SellingSpeedBadge(speed = property.sellingSpeed)

                    // Favorite / Save Toggle Button with bouncy spring animated heart icon
                    val heartScale by animateFloatAsState(
                        targetValue = if (property.isSaved) 1.25f else 1.0f,
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
                            .background(
                                color = if (property.isSaved) UrgencyFlame.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = if (property.isSaved) UrgencyFlame.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .testTag("save_button_${property.id}")
                            .testTag("favorite_button_${property.id}")
                    ) {
                        Icon(
                            imageVector = if (property.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (property.isSaved) "Remove from favorites" else "Add to favorites",
                            tint = if (property.isSaved) UrgencyFlame else Color.White,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(heartScale)
                        )
                    }
                }

                // Bottom overlay inside image: Price & Location
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = property.formattedPrice,
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (property.marketEstimate > property.price) {
                            Text(
                                text = property.formattedMarketEstimate,
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                            OpportunityPill(savingsText = property.formattedSavings)
                        }
                    }
                }
            }

            // Card Body Details
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                Text(
                    text = property.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Location & Distance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${property.location} • ${property.distanceKm} km away",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    VerificationBadge(level = property.verificationLevel)
                }

                // Specs: BHK, Bath, SqFt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (property.bedrooms > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Bed, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text("${property.bedrooms} BHK", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (property.bathrooms > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Bathtub, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text("${property.bathrooms} Bath", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.SquareFoot, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text("${property.areaSqFt} sq.ft", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }

                    UrgencyScoreRow(score = property.urgencyScore)
                }

                // Action Row: Contact Seller button with tactile feedback
                val contactInteraction = remember { MutableInteractionSource() }
                val isContactPressed by contactInteraction.collectIsPressedAsState()
                val contactScale by animateFloatAsState(
                    targetValue = if (isContactPressed) 0.96f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "contactScale_${property.id}"
                )

                OutlinedButton(
                    onClick = onContactSeller,
                    interactionSource = contactInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .scale(contactScale)
                        .testTag("contact_seller_button_${property.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Contact Seller",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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
        targetValue = if (isPressed) 0.975f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "urgentCardPressScale_${property.id}"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "urgentGlow_${property.id}")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgentGlowAlpha_${property.id}"
    )

    Card(
        modifier = modifier
            .width(295.dp)
            .scale(cardScale)
            .clip(RoundedCornerShape(24.dp))
            .background(CoolDarkCardGradient)
            .border(1.dp, UrgencyFlame.copy(alpha = glowAlpha * 0.6f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag("urgent_card_${property.id}"),
        colors = CardDefaults.cardColors(containerColor = UrgencyDarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Urgent Sale Pill & Expiry + Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SellingSpeedBadge(speed = property.sellingSpeed)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "48 hrs left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFED7AA)
                    )

                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color.White.copy(alpha = 0.14f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (property.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (property.isSaved) UrgencyFlame else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Title & Location
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = property.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Near ${property.location}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Bottom Section: Offer Price + Strikethrough & Thumbnail Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF334155),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Offer Price",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = property.formattedPrice,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (property.marketEstimate > property.price) {
                            Text(
                                text = property.formattedMarketEstimate,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }

                // Image Thumbnail Preview Box
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(10.dp))
                ) {
                    Image(
                        painter = painterResource(id = getDrawableResForName(property.imageResName)),
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // High Opportunity Pill & Savings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 HIGH OPPORTUNITY • 95% MATCH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFB923C),
                    letterSpacing = 0.4.sp
                )
                if (property.marketEstimate > property.price) {
                    OpportunityPill(savingsText = property.formattedSavings)
                }
            }

            // Quick Contact Button
            OutlinedButton(
                onClick = onContactSeller,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .testTag("urgent_contact_seller_button_${property.id}"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AccentGold.copy(alpha = 0.15f),
                    contentColor = AccentGold
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = AccentGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Contact Seller",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
            }
        }
    }
}
