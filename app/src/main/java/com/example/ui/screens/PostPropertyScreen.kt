package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import coil.compose.AsyncImage
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.ui.components.UrgencyScoreRow
import com.example.ui.theme.CardBorder
import com.example.ui.theme.FastSaleAmber
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.PrivateSaleDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UrgencyFlame
import com.example.ui.theme.UrgencyFlameContainer
import com.example.viewmodel.PostPropertyViewModel
import com.example.viewmodel.QuickNestViewModel

@Composable
fun PostPropertyScreen(
    postViewModel: PostPropertyViewModel,
    modifier: Modifier = Modifier
) {
    PostPropertyScreen(
        onPostProperty = { title, desc, cat, type, price, market, loc, beds, baths, sqft, speed, feat, priv, imageUri ->
            postViewModel.postNewProperty(
                title = title,
                description = desc,
                category = cat,
                propertyType = type,
                price = price,
                marketEstimate = market,
                location = loc,
                bedrooms = beds,
                bathrooms = baths,
                areaSqFt = sqft,
                speed = speed,
                features = feat,
                isPrivate = priv,
                imageUri = imageUri
            )
        },
        modifier = modifier
    )
}

@Composable
fun PostPropertyScreen(
    viewModel: QuickNestViewModel,
    modifier: Modifier = Modifier
) {
    PostPropertyScreen(
        onPostProperty = { title, desc, cat, type, price, market, loc, beds, baths, sqft, speed, feat, priv, imageUri ->
            viewModel.postNewProperty(
                title = title,
                description = desc,
                category = cat,
                propertyType = type,
                price = price,
                marketEstimate = market,
                location = loc,
                bedrooms = beds,
                bathrooms = baths,
                areaSqFt = sqft,
                speed = speed,
                features = feat,
                isPrivate = priv
            )
        },
        modifier = modifier
    )
}

@Composable
fun PostPropertyScreen(
    onPostProperty: (
        title: String,
        description: String,
        category: PropertyCategory,
        propertyType: String,
        price: Long,
        marketEstimate: Long,
        location: String,
        bedrooms: Int,
        bathrooms: Int,
        areaSqFt: Int,
        speed: SellingSpeed,
        features: List<String>,
        isPrivate: Boolean,
        imageUri: Uri?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PropertyCategory.BUY) }
    var propertyType by remember { mutableStateOf("Independent House") }
    var priceText by remember { mutableStateOf("") }
    var marketEstimateText by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Kottakuppam") }
    var bedrooms by remember { mutableStateOf("2") }
    var bathrooms by remember { mutableStateOf("2") }
    var areaSqFt by remember { mutableStateOf("1200") }
    var selectedSpeed by remember { mutableStateOf(SellingSpeed.FAST) }
    var protectPrivacy by remember { mutableStateOf(true) }
    var isPrivateListing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val availableFeatures = listOf("Covered Parking", "24/7 Water", "EB 3-Phase", "Private Garden", "Gated Security", "DTCP Approved", "Furnished")
    val selectedFeatures = remember { mutableStateOf(setOf("Covered Parking", "24/7 Water", "EB 3-Phase")) }

    // Dynamic Urgency Opportunity Score calculation
    val priceVal = priceText.toLongOrNull() ?: 0L
    val marketVal = marketEstimateText.toLongOrNull() ?: priceVal
    val savings = if (marketVal > priceVal && priceVal > 0) marketVal - priceVal else 0L

    val calculatedUrgencyScore by remember(selectedSpeed, savings) {
        derivedStateOf {
            var score = when (selectedSpeed) {
                SellingSpeed.URGENT -> 4
                SellingSpeed.FAST -> 3
                SellingSpeed.PRIVATE -> 4
                SellingSpeed.NORMAL -> 2
            }
            if (savings > 300000) score = (score + 1).coerceAtMost(5)
            score
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("post_property_form"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Post Property & Match Buyers",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "List for free and get matched with verified buyers in minutes",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // 1. SELLING SPEED SELECTOR (Core Innovation - Section 6)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
                        Text(
                            text = "⚡ Selling Speed",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        UrgencyScoreRow(score = calculatedUrgencyScore)
                    }

                    Text(
                        text = "How quickly do you need this property finalized?",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Speed cards
                    SellingSpeed.values().forEach { speed ->
                        val isSelected = selectedSpeed == speed
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) when (speed) {
                                SellingSpeed.URGENT -> UrgencyFlame.copy(alpha = 0.12f)
                                SellingSpeed.FAST -> FastSaleAmber.copy(alpha = 0.12f)
                                SellingSpeed.NORMAL -> NormalGreen.copy(alpha = 0.12f)
                                SellingSpeed.PRIVATE -> PrivateSaleDark.copy(alpha = 0.12f)
                            } else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) when (speed) {
                                    SellingSpeed.URGENT -> UrgencyFlame
                                    SellingSpeed.FAST -> FastSaleAmber
                                    SellingSpeed.NORMAL -> NormalGreen
                                    SellingSpeed.PRIVATE -> PrivateSaleDark
                                } else CardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedSpeed = speed
                                    if (speed == SellingSpeed.PRIVATE) isPrivateListing = true
                                }
                                .testTag("speed_option_${speed.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(speed.emoji, fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = speed.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Expected Timeline: ${speed.durationText}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = when (speed) {
                                            SellingSpeed.URGENT -> UrgencyFlame
                                            SellingSpeed.FAST -> FastSaleAmber
                                            SellingSpeed.NORMAL -> NormalGreen
                                            SellingSpeed.PRIVATE -> PrivateSaleDark
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CATEGORY & PROPERTY TYPE
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🏠 Category & Property Type",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Category row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(PropertyCategory.BUY, PropertyCategory.RENT, PropertyCategory.LEASE, PropertyCategory.LAND).forEach { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        selectedCategory = cat
                                        propertyType = when (cat) {
                                            PropertyCategory.LAND -> "Residential Plot"
                                            PropertyCategory.RENT -> "Rental House"
                                            PropertyCategory.LEASE -> "Villa for Lease"
                                            else -> "Independent House"
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.label}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else TextPrimary
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Property Title") },
                        placeholder = { Text("e.g. 2BHK Independent House near Serenity Beach") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Notes") },
                        placeholder = { Text("Mention road access, EB connection, water source, possession time...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        }

        // 3. PRICING & MARKET INTELLIGENCE (Section 7 & 28)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💰 Price & Market Valuation",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Your Price (₹)") },
                            placeholder = { Text("e.g. 3500000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = marketEstimateText,
                            onValueChange = { marketEstimateText = it },
                            label = { Text("Market Estimate (₹)") },
                            placeholder = { Text("e.g. 4000000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    if (savings > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = UrgencyFlameContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocalFireDepartment, null, tint = UrgencyFlame, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "🔥 High Opportunity Deal: ₹${savings / 100000.0} Lakhs below market estimate!",
                                    color = UrgencyFlame,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. LOCATION & PRIVACY SAFEGUARD (Section 18)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "📍 Location & Privacy Safeguard",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Kottakuppam", "Pondicherry", "Auroville", "Serenity Beach").forEach { loc ->
                            val isSel = location == loc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { location = loc }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = loc,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else TextPrimary
                                )
                            }
                        }
                    }

                    // Privacy Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛡️ Approximate Location (~500m)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hides exact house address publicly until visit request is accepted",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = protectPrivacy,
                            onCheckedChange = { protectPrivacy = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

                // 4b. PROPERTY PHOTOS & CLOUD STORAGE UPLOAD
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ðŸ“¸ Property Photographs",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Add real property photos to build buyer trust and increase urgent match scores",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (selectedImageUri != null) {
                        // Selected Photo Preview Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected property photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            // Remove / Change button
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedImageUri = null },
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp).size(18.dp)
                                )
                            }
                        }
                    } else {
                        // Upload Action Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add photo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Upload Property Photo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Select from Gallery or Camera â€¢ Auto uploaded to Cloud",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. FEATURES CHECKLIST
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨ Property Amenities",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    availableFeatures.chunked(2).forEach { rowFeatures ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowFeatures.forEach { feature ->
                                val isChecked = selectedFeatures.value.contains(feature)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val current = selectedFeatures.value.toMutableSet()
                                            if (isChecked) current.remove(feature) else current.add(feature)
                                            selectedFeatures.value = current
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            val current = selectedFeatures.value.toMutableSet()
                                            if (isChecked) current.remove(feature) else current.add(feature)
                                            selectedFeatures.value = current
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(text = feature, fontSize = 12.sp, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. SUBMIT & TRIGGER QUICKMATCH BUTTON
        item {
            Button(
                onClick = {
                    val finalPrice = priceText.toLongOrNull() ?: 3200000L
                    val finalMarket = marketEstimateText.toLongOrNull() ?: finalPrice
                    val finalTitle = title.ifBlank { "Urgent ${propertyType} in $location" }
                    val finalDesc = description.ifBlank { "Prime property with all verified amenities, direct road access, and clear title documentation." }

                    onPostProperty(
                        finalTitle,
                        finalDesc,
                        selectedCategory,
                        propertyType,
                        finalPrice,
                        finalMarket,
                        location,
                        bedrooms.toIntOrNull() ?: 2,
                        bathrooms.toIntOrNull() ?: 2,
                        areaSqFt.toIntOrNull() ?: 1200,
                        selectedSpeed,
                        selectedFeatures.value.toList(),
                        isPrivateListing,
                        selectedImageUri
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_post_property_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSpeed == SellingSpeed.URGENT) UrgencyFlame else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Post & Trigger QuickMatch Engine",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
