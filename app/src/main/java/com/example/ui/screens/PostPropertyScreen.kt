package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.BlueCorporate
import com.example.viewmodel.PostPropertyViewModel
import com.example.viewmodel.QuickNestViewModel

@Composable
fun PostPropertyScreen(postViewModel: PostPropertyViewModel, modifier: Modifier = Modifier) {
    val isSubmitting by postViewModel.isSubmitting.collectAsStateWithLifecycle()
    val postResult by postViewModel.postResult.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(postResult) {
        postResult?.onSuccess { prop ->
            successTitle = prop.title
            showSuccessDialog = true
        }?.onFailure { error ->
            errorMessage = error.localizedMessage ?: "Failed to submit listing. Please try again."
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                postViewModel.clearResult()
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Listing Submitted for Review",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "'$successTitle' has been sent to Ren Trust & Safety. Listings are verified to protect tenants and buyers before going live.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        postViewModel.clearResult()
                    },
                    modifier = Modifier.testTag("post_property_success_confirm_button")
                ) {
                    Text("OK")
                }
            }
        )
    }

    UnifiedListingForm(
        isSubmitting = isSubmitting,
        errorMessage = errorMessage,
        onClearError = { errorMessage = null },
        onPublish = { title, description, category, type, price, location, beds, baths, areaSqFt, furnishing, deposit, maintenance, included, available, landmark, tenants, features, photos ->
            postViewModel.postNewProperty(
                title = title,
                description = description,
                category = category,
                propertyType = type,
                price = price,
                marketEstimate = price,
                location = location,
                bedrooms = beds,
                bathrooms = baths,
                areaSqFt = areaSqFt,
                speed = SellingSpeed.NORMAL,
                features = features,
                furnishing = furnishing,
                securityDeposit = deposit,
                maintenanceAmount = maintenance,
                isMaintenanceIncluded = included,
                availableFrom = available,
                nearbyLandmark = landmark,
                tenantPreferences = tenants,
                imageUris = photos
            )
        },
        modifier = modifier
    )
}

/** Compatibility entry point used by legacy tests. */
@Composable
fun PostPropertyScreen(viewModel: QuickNestViewModel, modifier: Modifier = Modifier) {
    UnifiedListingForm(
        isSubmitting = false,
        errorMessage = null,
        onClearError = {},
        onPublish = { title, description, category, type, price, location, beds, baths, areaSqFt, _, _, _, _, _, _, _, features, _ ->
            viewModel.postNewProperty(
                title = title,
                description = description,
                category = category,
                propertyType = type,
                price = price,
                marketEstimate = price,
                location = location,
                bedrooms = beds,
                bathrooms = baths,
                areaSqFt = areaSqFt,
                speed = SellingSpeed.NORMAL,
                features = features,
                isPrivate = false
            )
        },
        modifier = modifier
    )
}

enum class LandAreaUnit(val label: String, val sqFtMultiplier: Double) {
    CENTS("Cents", 435.6),
    SQ_FT("Sq.Ft", 1.0),
    GROUNDS("Grounds", 2400.0),
    ACRES("Acres", 43560.0)
}

@Composable
private fun UnifiedListingForm(
    isSubmitting: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onPublish: (
        title: String,
        description: String,
        category: PropertyCategory,
        propertyType: String,
        price: Long,
        location: String,
        bedrooms: Int,
        bathrooms: Int,
        areaSqFt: Int,
        furnishing: String,
        securityDeposit: Long,
        maintenanceAmount: Long,
        isMaintenanceIncluded: Boolean,
        availableFrom: String,
        nearbyLandmark: String,
        tenantPreferences: List<String>,
        features: List<String>,
        photos: List<Uri>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mode / Category: Default is RENT
    var selectedCategory by remember { mutableStateOf(PropertyCategory.RENT) }

    // Common fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Pondicherry") }
    var locality by remember { mutableStateOf("Kottakuppam") }
    var landmark by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Rental specific fields
    var rentalType by remember { mutableStateOf("Apartment") }
    var rentAmount by remember { mutableStateOf("") }
    var securityDeposit by remember { mutableStateOf("") }
    var maintenanceAmount by remember { mutableStateOf("") }
    var maintenanceIncluded by remember { mutableStateOf(false) }
    var rentalBeds by remember { mutableStateOf("2") }
    var rentalBaths by remember { mutableStateOf("2") }
    var rentalAreaSqFt by remember { mutableStateOf("1100") }
    var furnishing by remember { mutableStateOf("Semi-furnished") }
    var availableFrom by remember { mutableStateOf("Available now") }
    var tenantPreferences by remember { mutableStateOf(setOf("Family", "Working professionals")) }
    var rentalAmenities by remember { mutableStateOf(setOf("24/7 Water", "EB Connection", "Car Parking")) }

    // Land specific fields
    var landType by remember { mutableStateOf("Residential Plot") }
    var landAreaInput by remember { mutableStateOf("5.0") }
    var landAreaUnit by remember { mutableStateOf(LandAreaUnit.CENTS) }
    var landAskingPrice by remember { mutableStateOf("") }
    var roadWidth by remember { mutableStateOf("30 ft Road") }
    var facingDirection by remember { mutableStateOf("North") }
    var landApprovals by remember { mutableStateOf(setOf("DTCP Approved", "Clear Patta / EC")) }
    var landFeatures by remember { mutableStateOf(setOf("Tar Road Access", "Compound Wall / Fenced", "Borewell / Water")) }

    // Buy (House / Villa sale) specific fields
    var houseType by remember { mutableStateOf("Independent House") }
    var housePrice by remember { mutableStateOf("") }
    var houseBeds by remember { mutableStateOf("3") }
    var houseBaths by remember { mutableStateOf("3") }
    var houseAreaSqFt by remember { mutableStateOf("1800") }
    var houseAmenities by remember { mutableStateOf(setOf("Car Parking", "24/7 Water", "Gated Security")) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        photos = (photos + uris).distinct().take(10)
    }

    val computedLocation = listOf(city.trim(), locality.trim())
        .filter(String::isNotEmpty)
        .joinToString(", ")

    // Derived Land Area in Sq.Ft. for database
    val parsedLandAreaValue = landAreaInput.toDoubleOrNull() ?: 0.0
    val computedLandAreaSqFt = (parsedLandAreaValue * landAreaUnit.sqFtMultiplier).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("post_property_scrollable_container"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = when (selectedCategory) {
                        PropertyCategory.RENT -> "List a Home for Rent"
                        PropertyCategory.LAND -> "List Land or Plot for Sale"
                        else -> "List a House for Sale"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Zero broker spam. Direct tenant & buyer inquiries via Ren Chat.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Error message if any
        if (!errorMessage.isNullOrBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearError) {
                            Text("✕", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // 1. Transaction Mode Tabs (Rent Home / Sell Land / Sell House)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "What would you like to list?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryOptionPill(
                            title = "Home for Rent",
                            icon = Icons.Default.Home,
                            selected = selectedCategory == PropertyCategory.RENT,
                            testTag = "post_property_category_rent",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = PropertyCategory.RENT
                        }

                        CategoryOptionPill(
                            title = "Land / Plot",
                            icon = Icons.Default.Landscape,
                            selected = selectedCategory == PropertyCategory.LAND,
                            testTag = "post_property_category_land",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = PropertyCategory.LAND
                        }

                        CategoryOptionPill(
                            title = "House for Sale",
                            icon = Icons.Default.Sell,
                            selected = selectedCategory == PropertyCategory.BUY,
                            testTag = "post_property_category_buy",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = PropertyCategory.BUY
                        }
                    }
                }
            }
        }

        // 2. Listing Title & General Info
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Listing Title & Overview", fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    PropertyCategory.RENT -> "e.g. Spacious 2 BHK Near Beach with Car Parking"
                                    PropertyCategory.LAND -> "e.g. DTCP Approved 5 Cents Plot on 30ft Road"
                                    else -> "e.g. Luxury 3 BHK Independent Villa"
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_property_title_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = {
                            Text(
                                when (selectedCategory) {
                                    PropertyCategory.RENT -> "Describe water availability, ventilation, furnishing, and community rules..."
                                    PropertyCategory.LAND -> "Describe road frontage, surrounding developments, fencing, and legal documentation..."
                                    else -> "Describe construction quality, approvals, water, and locality..."
                                }
                            )
                        },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_property_description_input")
                    )
                }
            }
        }

        // 3. Category-Specific Fields
        when (selectedCategory) {
            PropertyCategory.RENT -> {
                // RENTAL HOME FORM
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Rental Specifications", fontWeight = FontWeight.Bold, color = TextPrimary)

                            SelectionGrid(
                                label = "Home Type",
                                options = listOf("Apartment", "Independent House", "Villa", "Floor", "Studio/1RK", "PG/Room"),
                                selected = rentalType,
                                onSelect = { rentalType = it }
                            )

                            SelectionGrid(
                                label = "Furnishing",
                                options = listOf("Furnished", "Semi-furnished", "Unfurnished"),
                                selected = furnishing,
                                onSelect = { furnishing = it }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = rentalBeds,
                                    onValueChange = { rentalBeds = it },
                                    label = { Text("BHK (Beds)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("post_property_beds_input")
                                )
                                OutlinedTextField(
                                    value = rentalBaths,
                                    onValueChange = { rentalBaths = it },
                                    label = { Text("Bathrooms") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("post_property_baths_input")
                                )
                            }

                            OutlinedTextField(
                                value = rentalAreaSqFt,
                                onValueChange = { rentalAreaSqFt = it },
                                label = { Text("Carpet Area (sq.ft)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_property_area_input")
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Rent & Financials", fontWeight = FontWeight.Bold, color = TextPrimary)

                            OutlinedTextField(
                                value = rentAmount,
                                onValueChange = { rentAmount = it },
                                label = { Text("Monthly Rent (₹)") },
                                placeholder = { Text("e.g. 18000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_property_price_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = securityDeposit,
                                    onValueChange = { securityDeposit = it },
                                    label = { Text("Security Deposit (₹)") },
                                    placeholder = { Text("e.g. 100000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = maintenanceAmount,
                                    onValueChange = { maintenanceAmount = it },
                                    label = { Text("Maintenance (₹/mo)") },
                                    placeholder = { Text("e.g. 1500") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { maintenanceIncluded = !maintenanceIncluded }
                            ) {
                                Checkbox(
                                    checked = maintenanceIncluded,
                                    onCheckedChange = { maintenanceIncluded = it }
                                )
                                Text("Maintenance is included in the monthly rent", fontSize = 13.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Preferences & Amenities", fontWeight = FontWeight.Bold, color = TextPrimary)

                            SelectionGrid(
                                label = "Availability",
                                options = listOf("Available now", "From next month", "Flexible"),
                                selected = availableFrom,
                                onSelect = { availableFrom = it }
                            )

                            MultiSelectionGroup(
                                label = "Tenant Preferences",
                                options = listOf("Family", "Working professionals", "Bachelors", "Vegetarian only", "Pets allowed"),
                                selected = tenantPreferences,
                                onToggle = { option ->
                                    tenantPreferences = if (option in tenantPreferences) tenantPreferences - option else tenantPreferences + option
                                }
                            )

                            MultiSelectionGroup(
                                label = "Home Amenities",
                                options = listOf("24/7 Water", "EB Connection", "Car Parking", "Two Wheeler Parking", "Lift", "Balcony", "Power Backup", "Gated Security", "CCTV"),
                                selected = rentalAmenities,
                                onToggle = { option ->
                                    rentalAmenities = if (option in rentalAmenities) rentalAmenities - option else rentalAmenities + option
                                }
                            )
                        }
                    }
                }
            }

            PropertyCategory.LAND -> {
                // LAND / PLOT FORM
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Plot & Land Dimensions", fontWeight = FontWeight.Bold, color = TextPrimary)

                            SelectionGrid(
                                label = "Land Type",
                                options = listOf("Residential Plot", "Gated Layout Plot", "Farm / Agri Land", "Commercial Land", "Industrial Plot"),
                                selected = landType,
                                onSelect = { landType = it }
                            )

                            // Area Input with Unit Selector
                            Text("Plot Size & Unit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = landAreaInput,
                                    onValueChange = { landAreaInput = it },
                                    label = { Text("Area Size") },
                                    placeholder = { Text("e.g. 5.5") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("post_property_area_input")
                                )

                                Column(modifier = Modifier.weight(1.8f)) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(LandAreaUnit.values()) { unit ->
                                            FilterChip(
                                                selected = landAreaUnit == unit,
                                                onClick = { landAreaUnit = unit },
                                                label = { Text(unit.label, fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic Area Conversion helper badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Converted Area: $computedLandAreaSqFt sq.ft",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (landAreaUnit != LandAreaUnit.CENTS) {
                                        val cents = String.format("%.2f", computedLandAreaSqFt / 435.6)
                                        Text(
                                            text = "≈ $cents Cents",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = landAskingPrice,
                                onValueChange = { landAskingPrice = it },
                                label = { Text("Total Asking Price (₹)") },
                                placeholder = { Text("e.g. 2500000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_property_price_input")
                            )

                            // Price per unit calculator preview
                            val priceLong = landAskingPrice.toLongOrNull() ?: 0L
                            if (priceLong > 0 && parsedLandAreaValue > 0) {
                                val perUnit = (priceLong / parsedLandAreaValue).toLong()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Rate: ₹%,d / %s (≈ ₹%,d / sq.ft)".format(
                                            perUnit,
                                            landAreaUnit.label,
                                            if (computedLandAreaSqFt > 0) priceLong / computedLandAreaSqFt else 0
                                        ),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Approvals & Plot Features", fontWeight = FontWeight.Bold, color = TextPrimary)

                            SelectionGrid(
                                label = "Facing Direction",
                                options = listOf("North", "East", "South", "West", "North-East", "North-West"),
                                selected = facingDirection,
                                onSelect = { facingDirection = it }
                            )

                            SelectionGrid(
                                label = "Road Width",
                                options = listOf("20 ft Road", "30 ft Road", "40 ft Road", "60 ft Main Road"),
                                selected = roadWidth,
                                onSelect = { roadWidth = it }
                            )

                            MultiSelectionGroup(
                                label = "Legal & Layout Approvals",
                                options = listOf("DTCP Approved", "CMDA Approved", "RERA Registered", "Panchayat Approved", "Clear Patta / EC", "Freehold Title"),
                                selected = landApprovals,
                                onToggle = { opt ->
                                    landApprovals = if (opt in landApprovals) landApprovals - opt else landApprovals + opt
                                }
                            )

                            MultiSelectionGroup(
                                label = "Site Features",
                                options = listOf("Tar Road Access", "Compound Wall / Fenced", "Corner Plot", "Borewell / Water", "EB Line Available", "Clear Demarcation"),
                                selected = landFeatures,
                                onToggle = { opt ->
                                    landFeatures = if (opt in landFeatures) landFeatures - opt else landFeatures + opt
                                }
                            )
                        }
                    }
                }
            }

            PropertyCategory.BUY -> {
                // HOUSE / VILLA SALE FORM
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("House Specifications", fontWeight = FontWeight.Bold, color = TextPrimary)

                            SelectionGrid(
                                label = "House Type",
                                options = listOf("Independent House", "Villa", "Apartment", "Penthouse"),
                                selected = houseType,
                                onSelect = { houseType = it }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = houseBeds,
                                    onValueChange = { houseBeds = it },
                                    label = { Text("BHK (Beds)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = houseBaths,
                                    onValueChange = { houseBaths = it },
                                    label = { Text("Bathrooms") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = houseAreaSqFt,
                                onValueChange = { houseAreaSqFt = it },
                                label = { Text("Built-up Area (sq.ft)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_property_area_input")
                            )

                            OutlinedTextField(
                                value = housePrice,
                                onValueChange = { housePrice = it },
                                label = { Text("Asking Price (₹)") },
                                placeholder = { Text("e.g. 6500000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("post_property_price_input")
                            )

                            MultiSelectionGroup(
                                label = "Key Amenities",
                                options = listOf("Car Parking", "24/7 Water", "Gated Security", "CCTV", "Garden", "Solar Power"),
                                selected = houseAmenities,
                                onToggle = { opt ->
                                    houseAmenities = if (opt in houseAmenities) houseAmenities - opt else houseAmenities + opt
                                }
                            )
                        }
                    }
                }
            }
            else -> {}
        }

        // 4. Location Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Location Details", fontWeight = FontWeight.Bold, color = TextPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City / Town") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("post_property_city_input")
                        )
                        OutlinedTextField(
                            value = locality,
                            onValueChange = { locality = it },
                            label = { Text("Area / Locality") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("post_property_locality_input")
                        )
                    }

                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Nearby Landmark") },
                        placeholder = { Text("e.g. Near Serenity Beach Arch or Toll Plaza") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_property_landmark_input")
                    )

                    Text(
                        text = "Your exact street address is never shown publicly. Only approximate area (~500m) is visible to protect owner privacy.",
                        fontSize = 11.5.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 5. Photos Upload Section
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CardBorder),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Listing Photos", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = "Add up to 10 real photos. Listings with clear photos get 4x more verified responses.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = { photoPicker.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_property_add_photos_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Photos (${photos.size}/10)")
                    }

                    if (photos.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            photos.forEachIndexed { index, uri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected property photo",
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (index == 0) "Cover Photo (Primary)" else "Gallery Photo #${index + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { photos = photos - uri },
                                        modifier = Modifier.testTag("post_property_remove_photo_$index")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove photo", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Submit Button
        item {
            val effectivePrice = when (selectedCategory) {
                PropertyCategory.RENT -> rentAmount.toLongOrNull() ?: 0L
                PropertyCategory.LAND -> landAskingPrice.toLongOrNull() ?: 0L
                else -> housePrice.toLongOrNull() ?: 0L
            }

            val effectiveAreaSqFt = when (selectedCategory) {
                PropertyCategory.RENT -> rentalAreaSqFt.toIntOrNull() ?: 0
                PropertyCategory.LAND -> computedLandAreaSqFt
                else -> houseAreaSqFt.toIntOrNull() ?: 0
            }

            val isValid = title.isNotBlank() &&
                    effectivePrice > 0L &&
                    computedLocation.isNotBlank() &&
                    effectiveAreaSqFt > 0

            Button(
                onClick = {
                    when (selectedCategory) {
                        PropertyCategory.RENT -> {
                            onPublish(
                                title,
                                description,
                                PropertyCategory.RENT,
                                rentalType,
                                effectivePrice,
                                computedLocation,
                                rentalBeds.toIntOrNull() ?: 0,
                                rentalBaths.toIntOrNull() ?: 0,
                                effectiveAreaSqFt,
                                furnishing,
                                securityDeposit.toLongOrNull() ?: 0L,
                                maintenanceAmount.toLongOrNull() ?: 0L,
                                maintenanceIncluded,
                                availableFrom,
                                landmark,
                                tenantPreferences.toList(),
                                rentalAmenities.toList(),
                                photos
                            )
                        }
                        PropertyCategory.LAND -> {
                            // Land features include facing, road width, and approvals
                            val allLandFeatures = (landApprovals + landFeatures + listOf("Facing: $facingDirection", roadWidth)).toList()
                            onPublish(
                                title,
                                description,
                                PropertyCategory.LAND,
                                landType,
                                effectivePrice,
                                computedLocation,
                                0, // 0 beds
                                0, // 0 baths
                                effectiveAreaSqFt,
                                "",
                                0L,
                                0L,
                                false,
                                "Immediate Registration",
                                landmark,
                                emptyList(),
                                allLandFeatures,
                                photos
                            )
                        }
                        else -> {
                            onPublish(
                                title,
                                description,
                                PropertyCategory.BUY,
                                houseType,
                                effectivePrice,
                                computedLocation,
                                houseBeds.toIntOrNull() ?: 0,
                                houseBaths.toIntOrNull() ?: 0,
                                effectiveAreaSqFt,
                                "",
                                0L,
                                0L,
                                false,
                                "Ready to Move",
                                landmark,
                                emptyList(),
                                houseAmenities.toList(),
                                photos
                            )
                        }
                    }
                },
                enabled = isValid && !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueCorporate),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("post_property_publish_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Publishing listing...", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text(
                        text = when (selectedCategory) {
                            PropertyCategory.RENT -> "Publish Rental Listing for Review"
                            PropertyCategory.LAND -> "Publish Land for Review"
                            else -> "Publish Property for Review"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryOptionPill(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "pill_bg"
    )
    val contentColor = if (selected) Color.White else TextPrimary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun SelectionGrid(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        options.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { opt ->
                    val isSelected = opt == selected
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(opt) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opt,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiSelectionGroup(
    label: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { option ->
                    val isChecked = option in selected
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggle(option) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggle(option) },
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
